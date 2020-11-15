package com.alibaba.jvm.sandbox.repeater.broadcaster.mq;

import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.jvm.sandbox.repeater.plugin.Constants;
import com.alibaba.jvm.sandbox.repeater.plugin.annotation.ConfigActive;
import com.alibaba.jvm.sandbox.repeater.plugin.api.ConfigManager;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.AbstractBroadcaster;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultBroadcaster;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.ApplicationModel;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.HttpUtil;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.HttpUtil.Resp;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.RecordWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.SerializerWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RecordModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeatMeta;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeatModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterResult;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuangpeng
 * @since 2020/9/21
 */
@ConfigActive(value = Constants.METAQ)
@MetaInfServices(AbstractBroadcaster.class)
public class MQBroadcaster extends DefaultBroadcaster {

    private static final Logger LOGGER = LoggerFactory.getLogger(MQBroadcaster.class);

    @Override
    protected void broadcastRecord(RecordModel recordModel) {
        try {
            RecordWrapper wrapper = new RecordWrapper(recordModel);
            RecordDataPacket  recordDataPacket = RecordDataPacket.copyFrom(wrapper);
            byte[] data = JSON.toJSONBytes(recordDataPacket);
            MetaQConfig metaQConfig = MetaQConfig.getRecordConfig(ApplicationModel.instance().getConfig());
            MetaQInstance.getInstance().send(metaQConfig.getTopic(),metaQConfig.getTag(),recordModel.getTraceId(),data);
        } catch (Throwable throwable) {
            LOGGER.error("[Error-0000]-broadcast record failed", throwable);
        }

    }

    @Override
    protected void broadcastRepeat(RepeatModel record) {
        try {

            String body = SerializerWrapper.hessianSerialize(record);
            String url = record.getRepeatMeta().getReplayNotifyUrl();
            String traceId = record.getTraceId();
            if(StringUtils.isNotBlank(url)){
                HashMap<String, String> headers = Maps.newHashMap();
                headers.put("content-type", "application/json");
                Resp resp = HttpUtil.invokePostBody(url, headers, body);
                if (resp.isSuccess()) {
                    log.info("broadcast success,traceId={},resp={}", traceId, resp);
                } else {
                    log.info("broadcast failed ,traceId={},resp={}", traceId, resp);
                }
                return;
            }
            MetaQConfig metaQConfig = MetaQConfig.getReplayConfig(ApplicationModel.instance().getConfig());
            MetaQInstance.getInstance().send(metaQConfig.getTopic(),metaQConfig.getTag(),record.getTraceId(),body.getBytes());
        } catch (Throwable throwable) {
            LOGGER.error("[Error-0000]-broadcast record failed", throwable);
        }
    }

    @Override
    public RepeaterResult<RecordModel> pullRecord(RepeatMeta meta) {
        String url;
        if (StringUtils.isEmpty(meta.getDatasource())) {
            url = String.format(pullRecordUrl, meta.getAppName(), meta.getTraceId());
        } else {
            url = meta.getDatasource();
        }
        final HttpUtil.Resp resp = HttpUtil.doGet(url);
        if (!resp.isSuccess() || StringUtils.isEmpty(resp.getBody())) {
            log.info("get repeat data failed, datasource={}, response={}", meta.getDatasource(), resp);
            return RepeaterResult.builder().success(false).message("get repeat data failed").build();
        }
        List<RecordDataPacket> recordDataPackets = null;
        try {
            recordDataPackets = JSON.parseObject(resp.getBody(), new TypeReference<List<RecordDataPacket>>() {
            });
        } catch (Exception e) {
                log.info("invalid repeat data found, datasource={}, response={}", meta.getDatasource(), resp);
                return RepeaterResult.builder().success(false).message("repeat data found").build();
        }

        if(null != recordDataPackets && recordDataPackets.size() == 0){
            log.info("get repeat data failed, datasource={}, response={}", meta.getDatasource(), resp);
            return RepeaterResult.builder().success(false).message("get repeat data failed").build();
        }

        if(null != recordDataPackets && recordDataPackets.size() > 1){
            log.warn("found more than one invocation for url : {}",url);
        }

        RecordDataPacket dataPacket = recordDataPackets.get(0);

        // swap classloader cause this method will be call in target app thread
        ClassLoader swap = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(DefaultBroadcaster.class.getClassLoader());
            RecordWrapper recordWrapper = (RecordWrapper)SerializerWrapper.hessianDeserialize(dataPacket.getRecord());
            if (meta.isMock()) {
                if(null != recordWrapper.getEntranceInvocation()){
                    SerializerWrapper.inTimeDeserialize(recordWrapper.getEntranceInvocation());
                }

                if(CollectionUtils.isNotEmpty(recordWrapper.getSubInvocations())){
                    for (Invocation invocation : recordWrapper.getSubInvocations()) {
                        SerializerWrapper.inTimeDeserialize(invocation);
                    }
                }
            }
            return RepeaterResult.builder().success(true).message("operate success").data(recordWrapper.reTransform()).build();
        } catch (SerializeException e) {
            return RepeaterResult.builder().success(false).message(e.getMessage()).build();
        } finally {
            Thread.currentThread().setContextClassLoader(swap);
        }
    }
}
