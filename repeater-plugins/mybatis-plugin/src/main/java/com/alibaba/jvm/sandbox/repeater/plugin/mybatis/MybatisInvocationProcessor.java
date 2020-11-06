package com.alibaba.jvm.sandbox.repeater.plugin.mybatis;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.Event.Type;
import com.alibaba.jvm.sandbox.api.event.ReturnEvent;
import com.alibaba.jvm.sandbox.repeater.plugin.core.cache.RecordCache;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultInvocationProcessor;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.Serializer;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.SerializerWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Identity;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MybatisInvocation;
import com.alibaba.jvm.sandbox.repeater.plugin.mybatis.identity.DefaultMybatisIdentityFactory;
import com.alibaba.jvm.sandbox.repeater.plugin.mybatis.keyGen.DefaultKeyPropertiesParse;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 *
 * @author zhaoyb1990
 */
public class MybatisInvocationProcessor extends DefaultInvocationProcessor {

    protected final static Logger log = LoggerFactory.getLogger(MybatisInvocationProcessor.class);

    private static final ThreadLocal<Object> MAPPEDSTATEMENT_THREAD_LOCAL = new ThreadLocal();

    MybatisInvocationProcessor(InvokeType type) {
        super(type);
    }

    @Override
    public Identity assembleIdentity(BeforeEvent event) {
       return DefaultMybatisIdentityFactory.getInstance().getIdentity(event);
    }

    @Override
    public Object[] assembleRequest(BeforeEvent event) {
        if(event.javaClassName.equals("org.apache.ibatis.binding.MapperMethod")){
            return new Object[]{event.argumentArray[1]};
        }else{
            return event.argumentArray;
        }
    }

    @Override
    public Object assembleResponse(Event event) {
        //针对insert，将keyGen的信息存储起来
        handleInsertKeyGen(event);
        return super.assembleResponse(event);
    }

    private void handleInsertKeyGen(Event event) {
        if(event.type == Type.RETURN){
            ReturnEvent returnEvent = (ReturnEvent)event;
            MybatisInvocation invocation = (MybatisInvocation)RecordCache.getInvocation(returnEvent.invokeId);
            if(invocation.isInsert()){
                String[] keyProperties = getKeyProperties(invocation);
                if(null != keyProperties && keyProperties.length > 0){
                    parseKeyProperties(invocation,keyProperties);
                }
            }
        }
    }

    /**
     * 解析keyPorperties,放到invocation.keyPropertis2Value
     */
    private void parseKeyProperties(MybatisInvocation invocation, String[] keyProperties) {
        try {
            DefaultKeyPropertiesParse.getInstance().parse(invocation,keyProperties);
        } catch (SerializeException e) {
           log.error("SerializeException",e);
        }
    }

    public String[] getKeyProperties(MybatisInvocation invocation){
        Object mappedstatement = MAPPEDSTATEMENT_THREAD_LOCAL.get();
        if(null != mappedstatement){
            try {
                Object keyGenerator = MethodUtils.invokeMethod(mappedstatement,"getKeyGenerator");
                invocation.setKeyGenerator(keyGenerator.getClass().getCanonicalName());
                if(null != keyGenerator && !"org.apache.ibatis.executor.keygen.NoKeyGenerator".equals(invocation.getKeyGenerator())){
                    String[] keyProperties = (String[])MethodUtils.invokeMethod(mappedstatement,"getKeyProperties");
                    return keyProperties;
                }
            } catch (Exception e) {

            }
        }
        return null;
    }


    @Override
    public boolean inTimeSerializeRequest(Invocation invocation, BeforeEvent event) {
        return true;
    }

    public static void setMappedStatement(Object mappedStatement){
        MAPPEDSTATEMENT_THREAD_LOCAL.set(mappedStatement);
    }

    @Override
    public Object assembleMockResponse(BeforeEvent event, Invocation invocation) {
        MybatisInvocation mybatisInvocation = (MybatisInvocation)invocation;
        keyGeneratorMock(event.argumentArray, mybatisInvocation);
        return super.assembleMockResponse(event,invocation);
    }

    private void keyGeneratorMock(Object[] currentParam, MybatisInvocation mybatisInvocation) {
        if(null != mybatisInvocation.getRequestFinshSerialized() && mybatisInvocation.isInsert()){
            try {
                Object[] requestFinsh = SerializerWrapper.provider.provide(Serializer.Type.HESSIAN)
                    .deserialize(mybatisInvocation.getRequestFinshSerialized(),null, mybatisInvocation.getClassLoader());
                mybatisInvocation.setRequestFinsh(requestFinsh);
                DefaultKeyPropertiesParse.getInstance().mockKeyGenerator(currentParam,mybatisInvocation);
            } catch (SerializeException e) {
                log.error("SerializeException",e);
            }
        }
    }

    @Override
    public void clear(){
        MAPPEDSTATEMENT_THREAD_LOCAL.remove();
    }

}
