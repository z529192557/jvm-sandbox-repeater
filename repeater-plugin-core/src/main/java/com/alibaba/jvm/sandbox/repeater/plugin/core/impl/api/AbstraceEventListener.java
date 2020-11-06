package com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.Event.Type;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.ApplicationModel;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.TraceFactory;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.TraceContext;

/**
 * @author zhuangpeng
 * @since 2020/11/5
 */
public abstract class AbstraceEventListener implements EventListener {

    /**
     * 计算采样率
     *
     * @param event 事件
     * @return 是否采样
     */
    protected boolean sample(Event event) {
        final TraceContext context = TraceFactory.getContext();
        return context != null && context.isSampled();
    }

}
