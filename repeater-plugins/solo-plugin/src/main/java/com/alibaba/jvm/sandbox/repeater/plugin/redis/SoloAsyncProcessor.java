package com.alibaba.jvm.sandbox.repeater.plugin.redis;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.Event.Type;
import com.alibaba.jvm.sandbox.api.event.ReturnEvent;
import com.alibaba.jvm.sandbox.repeater.plugin.core.cache.RecordCache;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultInvocationListener;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultInvocationProcessor;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.TraceFactory;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.SerializerWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * solo异步调用处理
 *
 * @author zhuangpeng
 * @since 2020/10/13
 */
public class SoloAsyncProcessor extends DefaultInvocationProcessor {

    private final static Logger log = LoggerFactory.getLogger(DefaultInvocationListener.class);

    private volatile Class<?> soloFutureClass = null;

    private volatile Class<?> nkvFutureClass = null;

    private volatile Class<?> packetWrapperClass = null;


    private static final Cache<Future, Invocation> FUTURE_CACHE = CacheBuilder
        .newBuilder()
        .maximumSize(4096)
        .expireAfterWrite(30, TimeUnit.SECONDS).build();

    private static final Cache<Future, Object> FUTURE_RESULT_CACHE = CacheBuilder
        .newBuilder()
        .maximumSize(4096)
        .expireAfterWrite(30, TimeUnit.SECONDS).build();

    private Map<String,Class<?>> methodReturnTypeMap = new ConcurrentHashMap<>();

    public SoloAsyncProcessor(InvokeType type) {
        super(type);
    }

    @Override
    public Object assembleResponse(Event event) {
        if (event.type == Type.RETURN) {
            ReturnEvent returnEvent = ((ReturnEvent) event);
            Object returnObj = returnEvent.object;
            if(returnObj instanceof Future){
               Invocation invocation = RecordCache.getInvocation(returnEvent.invokeId);
               FUTURE_CACHE.put((Future)returnObj,invocation);
            }
        }
        return null;
    }

    public static void setFutureResult(Future future,Object o){
        Invocation invocation = FUTURE_CACHE.getIfPresent(future);
        if(null != invocation){
            invocation.setResponse(o);
        }
        try {
            SerializerWrapper.inTimeSerialize(invocation);
        } catch (SerializeException e) {
            TraceFactory.getContext().setSampled(false);
            log.error("Error occurred serialize", e);
        }
    }

    public static Object getFutureResult(Future future){
        return FUTURE_RESULT_CACHE.getIfPresent(future);
    }

    @Override
    public Object assembleMockResponse(BeforeEvent event, Invocation invocation) throws IllegalArgumentException {
        // 返回一个SoloResultFutureImpl
        try {
            if (soloFutureClass == null) {
                soloFutureClass = event.javaClassLoader.loadClass(
                    "com.netease.backend.solo.client.rpc.future.SoloResultFutureImpl");
            }
            if (nkvFutureClass == null) {
                nkvFutureClass = event.javaClassLoader.loadClass(
                    "com.netease.backend.solo.client.rpc.net.NkvFuture");
            }

            if (packetWrapperClass == null) {
                packetWrapperClass = event.javaClassLoader.loadClass(
                    "com.netease.backend.solo.client.rpc.protocol.tair2_3.PacketWrapper");
            }



            Object mockResponse = invocation.getResponse();
            Object packetWrapper = MethodUtils.invokeStaticMethod(packetWrapperClass,"buildWithBody",-2, invocation.getResponse());
            MethodUtils.invokeMethod(packetWrapper,"setBody",invocation.getResponse());
            Object nkvFuture = nkvFutureClass.newInstance();
            ReentrantLock lock = new ReentrantLock();
            Condition cond = lock.newCondition();
            MethodUtils.invokeMethod(nkvFuture,"setLock",lock);
            MethodUtils.invokeMethod(nkvFuture,"setCond",cond);
            MethodUtils.invokeMethod(nkvFuture,"setValue",packetWrapper);
            Constructor<?> constructor = soloFutureClass.getConstructor(nkvFutureClass,Class.class);
            Future future = (Future)constructor.newInstance(nkvFuture,mockResponse.getClass());
            FUTURE_RESULT_CACHE.put(future,mockResponse);
            return future;
        } catch (Exception e) {
            // impossible
            return null;
        }
    }
}
