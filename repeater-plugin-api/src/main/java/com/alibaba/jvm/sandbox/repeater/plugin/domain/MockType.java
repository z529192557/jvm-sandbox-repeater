package com.alibaba.jvm.sandbox.repeater.plugin.domain;

/**
 * @author zhuangpeng
 * @since 2020/11/28
 */
/**
 * Mock类型
 */
public enum MockType {
    /**
     * 部分Mock
     */
    MOCK_PART("MOCK_PART"),
    /**
     * 部分不Mock
     */
    NOT_MOCK_PART("NOT_MOCK_PART"),

    /**
     * mock异常
     */
    EXCEPTION_MOCK("EXCEPTION_MOCK");

    private String code;

    MockType(String code) {
        this.code = code;
    }
}
