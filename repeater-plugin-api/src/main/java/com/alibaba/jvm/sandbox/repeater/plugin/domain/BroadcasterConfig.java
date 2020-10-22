package com.alibaba.jvm.sandbox.repeater.plugin.domain;

import java.util.Map;

/**
 * @author zhuangpeng
 * @since 2020/10/21
 */
public class BroadcasterConfig {

    /**
     * 消息广播实现
     */
    private String name = "";

    /**
     *  消息广播元配置，topic,tag 等
     */
    private Map<String,String> metaConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getMetaConfig() {
        return metaConfig;
    }

    public void setMetaConfig(Map<String, String> metaConfig) {
        this.metaConfig = metaConfig;
    }
}
