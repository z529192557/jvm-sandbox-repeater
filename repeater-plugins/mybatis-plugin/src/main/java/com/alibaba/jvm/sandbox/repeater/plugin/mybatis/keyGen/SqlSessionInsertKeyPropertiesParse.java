package com.alibaba.jvm.sandbox.repeater.plugin.mybatis.keyGen;

import java.util.Map;

import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.Serializer.Type;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.SerializerWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MybatisInvocation;

/**
 *
 * sqlSession.insert(stmt,param)
 * sqlSession.insert(stmt)
 *
 * @author zhuangpeng
 * @since 2020/11/5
 */
public class SqlSessionInsertKeyPropertiesParse extends AbstractKeyPropertiesParse {

    public void parse(MybatisInvocation invocation, String[] keyPropertise) throws SerializeException {
        Object[] request = invocation.getRequest();
        if(null != request && request.length > 1){
            invocation.setRequestFinsh(request);
            invocation.setRequestFinshSerialized(SerializerWrapper.provider.provide(Type.HESSIAN)
                .serialize2String(invocation.getResponse(), invocation.getClassLoader()));
            Object param = request[1];
            Map<String,Object> valueMap = getKeyPropertiesValue(param,keyPropertise);
            invocation.setKeyProperties2Value(valueMap);
        }
    }

    @Override
    public void mockKeyGenerator(Object[] currentParam,MybatisInvocation invocation) {
        if(currentParam.length > 1){
            setValueForKeyProperties(currentParam[1],invocation.getKeyProperties2Value());
        }
    }
}
