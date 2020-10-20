package com.alibaba.jvm.sandbox.repeater.plugin.redis;

import java.util.List;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.repeater.plugin.api.InvocationProcessor;
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
public class SoloAsyncPlugin extends SoloPlugin {

    String[] ASYNC_COMMANDS ={"mcOpsAsync","mcReplaceAsync","incrMultiCountsAsync","decrMultiCountsAsync","decMultiCountsAsync","setMultiCountsAsync","putAsync","putWithBizVersionAsync","removeAsync","invalidAsync","expireAsync","batchPutAsync","batchRemoveAsync","batchInvalidAsync","prefixPutAsync","prefixRemoveAsync","prefixInvalidAsync","incrAsync","decrAsync","incrBoundedAsync","decrBoundedAsync","lockAsync","unlockAsync","setCountAsync","prefixRemovesAsync","prefixInvalidsAsync","prefixPutsAsync","prefixSetCountsAsync","prefixIncrsAsync","prefixDecrsAsync","prefixIncrsBoundedAsync","prefixDecrsBoundedAsync"};


    @Override
    protected List<EnhanceModel> getEnhanceModels() {
        EnhanceModel multiSoloManager = EnhanceModel.builder()
            .classPattern("com.netease.haitao.solo.wrapper.SoloClientWriteWrapper")
            .methodPatterns(EnhanceModel.MethodPattern.transform(
                ArrayUtils.addAll(ASYNC_COMMANDS)))
            .watchTypes(Event.Type.BEFORE, Event.Type.RETURN, Event.Type.THROWS)
            .build();

        return Lists.newArrayList(multiSoloManager);
    }

    @Override
    protected InvocationProcessor getInvocationProcessor() {
        return new SoloAsyncProcessor(getType());
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
}
