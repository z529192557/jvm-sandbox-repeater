package com.alibaba.jvm.sandbox.repeater.plugin.mybatis;

import java.util.List;

import com.alibaba.jvm.sandbox.api.event.Event.Type;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.api.InvocationListener;
import com.alibaba.jvm.sandbox.repeater.plugin.api.InvocationProcessor;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.AbstractInvokePluginAdapter;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultEventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.EnhanceModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;
import com.alibaba.jvm.sandbox.repeater.plugin.spi.InvokePlugin;

import com.google.common.collect.Lists;
import org.kohsuke.MetaInfServices;

/**
 *
 * @author zhuangpeng
 * @since 2020/11/5
 */
@MetaInfServices(InvokePlugin.class)
public class MybatisConfigurationPlugin extends AbstractInvokePluginAdapter {

    @Override
    protected List<EnhanceModel> getEnhanceModels() {
        EnhanceModel em = EnhanceModel.builder()
            .classPattern("org.apache.ibatis.session.Configuration")
            .methodPatterns(EnhanceModel.MethodPattern.transform("getMappedStatement"))
            .watchTypes(Type.BEFORE, Type.RETURN, Type.THROWS)
            .build();
        return Lists.newArrayList(em);
    }

    @Override
    protected InvocationProcessor getInvocationProcessor() {
        return null;
    }

    @Override
    public InvokeType getType() {
        return InvokeType.MYBATIS;
    }

    @Override
    public String identity() {
        return InvokeType.MYBATIS.name();
    }

    @Override
    public boolean isEntrance() {
        return false;
    }

    @Override
    protected EventListener getEventListener(InvocationListener listener) {
        return new MybatisConfigurationEventListener();
    }
}
