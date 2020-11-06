package com.alibaba.jvm.sandbox.repeater.plugin.mybatis.keyGen;

import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MybatisInvocation;

/**
 * @author zhuangpeng
 * @since 2020/11/5
 */
public interface KeyPropertiesParse {

    /**
     * 解析keyProperties
     */
    void parse(MybatisInvocation invocation, String... keyPropertise) throws SerializeException;

    void mockKeyGenerator(Object[] currentParam,MybatisInvocation mybatisInvocation);
}
