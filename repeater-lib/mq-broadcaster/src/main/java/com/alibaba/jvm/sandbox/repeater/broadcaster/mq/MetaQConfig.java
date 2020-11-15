package com.alibaba.jvm.sandbox.repeater.broadcaster.mq;

import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterConfig;

/**
 * @author zhuangpeng
 * @since 2020/10/21
 */
public class MetaQConfig {

    /**
     * meta topic
     */
    private String topic;

    /**
     * meta tag
     */
    private String tag;


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "MetaQConfig{" +
            "topic='" + topic + '\'' +
            ", tag='" + tag + '\'' +
            '}';
    }

    public static MetaQConfig getRecordConfig(RepeaterConfig config) {
        MetaQConfig metaQConfig = new MetaQConfig();
        metaQConfig.setTopic(getMetaConfigValue(config,MessageConfig.METAQ_RECORD_BROADCASTER_TOPIC,MessageConfig.DEFAULT_TOPIC_NAME));
        metaQConfig.setTag(getMetaConfigValue(config,MessageConfig.METAQ_RECORD_BROADCASTER_TAG,MessageConfig.DEFAULT_RECORD_TAG_NAME));
        return metaQConfig;
    }

    public static MetaQConfig getReplayConfig(RepeaterConfig config) {
        MetaQConfig metaQConfig = new MetaQConfig();
        metaQConfig.setTopic(getMetaConfigValue(config,MessageConfig.METAQ_REPLAY_BROADCASTER_TOPIC,MessageConfig.DEFAULT_TOPIC_NAME));
        metaQConfig.setTag(getMetaConfigValue(config,MessageConfig.METAQ_REPLAY_BROADCASTER_TAG,MessageConfig.DEFAULT_REPLAY_TAG_NAME));
        return metaQConfig;
    }


    public static String getMetaConfigValue(RepeaterConfig config ,String key ,String defaultValue){
        String value = null;
        if(null != config.getBroadcasterConfig() && null != config.getBroadcasterConfig().getMetaConfig()){
            value = config.getBroadcasterConfig().getMetaConfig().get(key);
        }
        if(null == value){
            value = defaultValue;
        }
        return value;
    }
}
