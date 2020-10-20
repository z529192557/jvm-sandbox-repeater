package com.alibaba.jvm.sandbox.repeater.plugin.redis;

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
import java.util.*;

/**
 * {@link SoloPlugin} haitao-solo的java插件
 *
 * 拦截 SoloClientWriteWrapper和SoloClientReadWrapper,
 * MultiSoloManagerImpl的getWithLocalCache、batchGetWithLocalCache
 *
 * @author zhuangpeng
 */
@MetaInfServices(InvokePlugin.class)
public class SoloPlugin extends AbstractInvokePluginAdapter {

    private volatile  EventListener sharedEventListner = null;

    String[] READ_SYNC_COMMANDS = {"get","getWithLocalCache","batchGetWithLocalCache","batchGet","getAndTouch","getRange","getMultiCounts","batchGetCount","prefixGet","prefixGets","lockStatus"};

    String[] READ_SYNC_COMMANDS_LOCALCACHE = {"getWithLocalCache","batchGetWithLocalCache"};


    String[] WRITE_SYNC_COMMANDS = {"put","putWithBizVersion","remove","invalid","expire","delRange","delRangeReverse","mcReplcae","setMultiCounts","incrMultiCounts","decrMultiCounts","batchPut","batchRemove","batchInvalid","prefixPut","prefixRemove","prefixInvalid","incr","decr","incrBounded","decrBounded","lock","unlock","setCount","putIfNoExist","prefixRemoves","prefixInvalids","prefixPuts","prefixSetCounts","prefixIncrs","prefixDecrs","prefixIncrsBounded","compareAndPut","mcOps","prefixDecrsBounded","incMultiCounts","decMultiCounts",};


    @Override
    protected List<EnhanceModel> getEnhanceModels() {
        EnhanceModel soloClientWriteWrapper = EnhanceModel.builder()
                .classPattern("com.netease.haitao.solo.wrapper.SoloClientWriteWrapper")
                .methodPatterns(EnhanceModel.MethodPattern.transform(WRITE_SYNC_COMMANDS))
                .watchTypes(Event.Type.BEFORE, Event.Type.RETURN, Event.Type.THROWS)
                .build();
        EnhanceModel multiSoloManagerImpl = EnhanceModel.builder()
            .classPattern("com.netease.haitao.solo.impl.MultiSoloManagerImpl")
            .methodPatterns(EnhanceModel.MethodPattern.transform(READ_SYNC_COMMANDS_LOCALCACHE))
            .watchTypes(Event.Type.BEFORE, Event.Type.RETURN, Event.Type.THROWS)
            .build();

        EnhanceModel soloClientReadWrapper = EnhanceModel.builder()
            .classPattern("com.netease.haitao.solo.wrapper.SoloClientReadWrapper")
            .methodPatterns(EnhanceModel.MethodPattern.transform(READ_SYNC_COMMANDS))
            .watchTypes(Event.Type.BEFORE, Event.Type.RETURN, Event.Type.THROWS)
            .build();

        return Lists.newArrayList(soloClientWriteWrapper,multiSoloManagerImpl,soloClientReadWrapper);
    }

    @Override
    protected InvocationProcessor getInvocationProcessor() {
        return new SoloProcessor(getType());
    }

    @Override
    public InvokeType getType() {
        return InvokeType.SOLO;
    }

    @Override
    public String identity() {
        return InvokeType.SOLO.name();
    }

    @Override
    public boolean isEntrance() {
        return false;
    }

    @Override
    protected EventListener getEventListener(InvocationListener listener) {
        if(null == sharedEventListner){
            synchronized (this){
                if(null == sharedEventListner){
                    sharedEventListner = new DefaultEventListener(getType(), isEntrance(), listener, getInvocationProcessor());
                    return sharedEventListner;
                }
            }
        }
        return sharedEventListner;
    }

}
