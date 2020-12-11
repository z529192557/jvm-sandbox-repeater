package com.alibaba.jvm.sandbox.repeater.plugin.domain;

/**
 * @author zhuangpeng
 * @since 2020/12/11
 */
public enum DubboAttachment {

    ASYNC("async");
    private String name;

    DubboAttachment(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public String getName() {
        return name;
    }
}
