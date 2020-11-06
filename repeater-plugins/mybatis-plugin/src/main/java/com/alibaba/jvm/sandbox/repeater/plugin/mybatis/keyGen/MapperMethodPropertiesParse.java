package com.alibaba.jvm.sandbox.repeater.plugin.mybatis.keyGen;

import java.util.Map;

import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.Serializer.Type;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.SerializerWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MybatisInvocation;

/**
 *
 * MapperMethod.execute(SqlSession sqlSession, Object[] args) {
 *
 * @author zhuangpeng
 * @since 2020/11/5
 */
public class MapperMethodPropertiesParse extends AbstractKeyPropertiesParse{

    @Override
    public void parse(MybatisInvocation invocation, String[] keyPropertise) throws SerializeException {
        Object[] request = invocation.getRequest();
        invocation.setRequestFinsh(request);
        invocation.setRequestFinshSerialized(SerializerWrapper.provider.provide(Type.HESSIAN)
            .serialize2String(request, invocation.getClassLoader()));
        if(null != request && request.length>0){
            Map<String,Object> valueMap = getKeyPropertiesValue(request[0],keyPropertise);
            invocation.setKeyProperties2Value(valueMap);
        }
    }


    @Override
    public void mockKeyGenerator(Object[] currentParam,MybatisInvocation invocation){
        setValueForKeyProperties(currentParam[1],invocation.getKeyProperties2Value());
    }
}
