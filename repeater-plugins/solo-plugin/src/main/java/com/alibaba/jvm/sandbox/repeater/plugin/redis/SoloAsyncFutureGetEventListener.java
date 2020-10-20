package com.alibaba.jvm.sandbox.repeater.plugin.redis;

import java.util.concurrent.Future;

import com.alibaba.jvm.sandbox.api.ProcessController;
import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.Event.Type;
import com.alibaba.jvm.sandbox.api.event.ReturnEvent;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.core.cache.RepeatCache;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.TraceFactory;

/**
 * @author zhuangpeng
 * @since 2020/10/13
 */
public class SoloAsyncFutureGetEventListener implements EventListener {

    private static final ThreadLocal<Future> FUTURE_THREAD_LOCAL = new ThreadLocal<>();
    @Override
    public void onEvent(Event event) throws Throwable {
        if (RepeatCache.isRepeatFlow(TraceFactory.getTraceId())) {
            if(event.type == Type.BEFORE){
                BeforeEvent returnEvent = (BeforeEvent)event;
                Object o = SoloAsyncProcessor.getFutureResult((Future)returnEvent.target);
                if(null != o){
                    ProcessController.returnImmediately(o);
                }
            }
        }else{
            if(event.type == Type.BEFORE){
                BeforeEvent returnEvent = (BeforeEvent)event;
                if(returnEvent.target instanceof Future){
                    FUTURE_THREAD_LOCAL.set((Future)returnEvent.target);
                }
            }

            if(event.type == Type.RETURN || event.type == Type.THROWS){
                Future future = FUTURE_THREAD_LOCAL.get();
                if(null != future){
                    ReturnEvent returnEvent = (ReturnEvent)event;
                    SoloAsyncProcessor.setFutureResult(future,returnEvent.object);
                    FUTURE_THREAD_LOCAL.remove();
                }
            }
        }
    }
}
