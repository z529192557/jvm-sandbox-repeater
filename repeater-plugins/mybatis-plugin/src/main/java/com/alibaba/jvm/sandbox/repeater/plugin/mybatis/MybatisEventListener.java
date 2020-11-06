package com.alibaba.jvm.sandbox.repeater.plugin.mybatis;

import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.jvm.sandbox.api.ProcessControlException;
import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.Event.Type;
import com.alibaba.jvm.sandbox.api.event.InvokeEvent;
import com.alibaba.jvm.sandbox.api.event.ReturnEvent;
import com.alibaba.jvm.sandbox.api.event.ThrowsEvent;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.api.InvocationListener;
import com.alibaba.jvm.sandbox.repeater.plugin.api.InvocationProcessor;
import com.alibaba.jvm.sandbox.repeater.plugin.core.bridge.ClassloaderBridge;
import com.alibaba.jvm.sandbox.repeater.plugin.core.cache.RecordCache;
import com.alibaba.jvm.sandbox.repeater.plugin.core.cache.RepeatCache;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultEventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.ApplicationModel;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.SequenceGenerator;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.TraceFactory;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.SerializerWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MybatisInvocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.TraceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MybatisEventListener} mybatis默认的事件监听器实现类
 *
 * 处理Mybatis的 keyGen逻辑
 *
 * @author zhaoyb1990
 */
public class MybatisEventListener extends DefaultEventListener {

    public MybatisEventListener(InvokeType invokeType, boolean entrance,
        InvocationListener listener, InvocationProcessor processor) {
        super(invokeType, entrance, listener, processor);
    }

    @Override
    protected Invocation initInvocation(BeforeEvent beforeEvent) {
        MybatisInvocation invocation = new MybatisInvocation();
        invocation.setHookClass(beforeEvent.javaClassName);
        return invocation;
    }
}
