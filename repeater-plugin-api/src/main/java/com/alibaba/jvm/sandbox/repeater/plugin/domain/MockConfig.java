package com.alibaba.jvm.sandbox.repeater.plugin.domain;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author zhuangpeng
 * @since 2020/11/28
 */
public class MockConfig {

    /**
     * mock类型
     */
    private MockType mockType;

    /**
     * 部分mock/部分不mock的子调用标志
     */
    private List<String> identities = Lists.newArrayList();

    /**
     * 模拟异常的Identify | 分隔
     */
    private String mockExceptionIndentities;

    /**
     * 自定义的groovy脚本
     * key identity
     * value Groovy script
     */
    public Map<String, String> identfy2GroovyForMock = Maps.newHashMap();

    public MockType getMockType() {
        return mockType;
    }

    public void setMockType(MockType mockType) {
        this.mockType = mockType;
    }

    public List<String> getIdentities() {
        return identities;
    }

    public void setIdentities(List<String> identities) {
        this.identities = identities;
    }

    public Map<String, String> getIdentfy2GroovyForMock() {
        return identfy2GroovyForMock;
    }

    public void setIdentfy2GroovyForMock(Map<String, String> identfy2GroovyForMock) {
        this.identfy2GroovyForMock = identfy2GroovyForMock;
    }

    public String getMockExceptionIndentities() {
        return mockExceptionIndentities;
    }

    public void setMockExceptionIndentities(String mockExceptionIndentities) {
        this.mockExceptionIndentities = mockExceptionIndentities;
    }
}
