package com.alibaba.jvm.sandbox.repeater.broadcaster.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.jvm.sandbox.repeater.plugin.Constants;
import com.alibaba.jvm.sandbox.repeater.plugin.annotation.ConfigActive;
import com.alibaba.jvm.sandbox.repeater.plugin.api.ConfigManager;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.AbstractBroadcaster;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultBroadcaster;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.ApplicationModel;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.RecordWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.SerializerWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RecordModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeatModel;
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
            byte[] bytes = JSON.toJSONBytes(wrapper);
            MetaQConfig metaQConfig = MetaQConfig.getRecordConfig(ApplicationModel.instance().getConfig());
            MetaQInstance.getInstance().send(metaQConfig.getTopic(),metaQConfig.getTag(),recordModel.getTraceId(),bytes);
        } catch (Throwable throwable) {
            LOGGER.error("[Error-0000]-broadcast record failed", throwable);
        }

    }

    @Override
    protected void broadcastRepeat(RepeatModel record) {
        try {
            byte[] bytes = JSON.toJSONBytes(record);
            MetaQConfig metaQConfig = MetaQConfig.getReplayConfig(ApplicationModel.instance().getConfig());
            MetaQInstance.getInstance().send(metaQConfig.getTopic(),metaQConfig.getTag(),record.getTraceId(),bytes);
        } catch (Throwable throwable) {
            LOGGER.error("[Error-0000]-broadcast record failed", throwable);
        }
    }
}
