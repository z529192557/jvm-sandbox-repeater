package com.alibaba.jvm.sandbox.repeater.broadcaster.mq;

import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.RecordWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.SerializerWrapper;

/**
 * @author zhuangpeng
 * @since 2020/11/9
 */
public class RecordDataPacket {

    /**
     * traceID;
     */
    private String traceId;

    /**
     * 入口描述
     */
    private String entranceDesc;
    /**
     * 序列化后的recordzhi
     */
    private String record;
    /**
     * appName
     */
    private String appName;
    /**
     * 环境
     */
    private String environment;

    /**
     * 采集的机器ip
     */
    private String host;

    /**
     * 时间戳
     */
    private Long timestamp;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getEntranceDesc() {
        return entranceDesc;
    }

    public void setEntranceDesc(String entranceDesc) {
        this.entranceDesc = entranceDesc;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public static RecordDataPacket copyFrom(RecordWrapper recordWrapper) throws SerializeException {
        RecordDataPacket packet = new RecordDataPacket();
        packet.traceId = recordWrapper.getTraceId();
        packet.appName = recordWrapper.getAppName();
        packet.entranceDesc = recordWrapper.getEntranceDesc();
        packet.environment = recordWrapper.getEnvironment();
        packet.record = SerializerWrapper.hessianSerialize(recordWrapper);
        packet.timestamp = recordWrapper.getTimestamp();
        packet.host = recordWrapper.getHost();
        return packet;
    }
}
