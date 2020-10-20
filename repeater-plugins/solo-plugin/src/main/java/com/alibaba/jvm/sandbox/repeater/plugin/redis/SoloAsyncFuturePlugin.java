package com.alibaba.jvm.sandbox.repeater.plugin.redis;

import java.util.List;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.api.InvocationListener;
import com.alibaba.jvm.sandbox.repeater.plugin.api.InvocationProcessor;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.AbstractInvokePluginAdapter;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultEventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.EnhanceModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;
import com.alibaba.jvm.sandbox.repeater.plugin.spi.InvokePlugin;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.kohsuke.MetaInfServices;

/**
 * @author zhuangpeng
 * @since 2020/10/13
 */
@MetaInfServices(InvokePlugin.class)
public class SoloAsyncFuturePlugin extends SoloAsyncPlugin {

    @Override
    protected List<EnhanceModel> getEnhanceModels() {
        EnhanceModel multiSoloManager = EnhanceModel.builder()
            .classPattern("com.netease.backend.solo.client.rpc.future.SoloResultFutureImpl")
            .methodPatterns(EnhanceModel.MethodPattern.transform("innerGet"))
            .watchTypes(Event.Type.BEFORE, Event.Type.RETURN, Event.Type.THROWS)
            .build();

        return Lists.newArrayList(multiSoloManager);
    }

    @Override
    protected InvocationProcessor getInvocationProcessor() {
        return null;
    }

    @Override
    public InvokeType getType() {
        return InvokeType.SOLOASYNC;
    }

    @Override
    public String identity() {
        return InvokeType.SOLOASYNC.name();
    }

    @Override
    public boolean isEntrance() {
        return false;
    }

    @Override
    protected EventListener getEventListener(InvocationListener listener) {
        return new SoloAsyncFutureGetEventListener();
    }
}
