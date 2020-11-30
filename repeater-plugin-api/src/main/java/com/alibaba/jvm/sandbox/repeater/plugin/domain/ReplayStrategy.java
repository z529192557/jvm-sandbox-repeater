package com.alibaba.jvm.sandbox.repeater.plugin.domain;

import sun.font.Script;

/**
 *
 * 回放策略配置
 *
 * 这里存放各种回放中的配置
 *
 * 1.部分子调用mock;
 * 2.部分子调用不mock
 * 3.指定identify抛出异常
 * 4.特殊mock行为
 *
 * @author zhuangpeng
 * @since 2020/11/28
 */
public class ReplayStrategy {

    /**
     * 针对mock的配置
     */
    MockConfig mockConfig;


    public MockConfig getMockConfig() {
        return mockConfig;
    }

    public void setMockConfig(MockConfig mockConfig) {
        this.mockConfig = mockConfig;
    }
}
