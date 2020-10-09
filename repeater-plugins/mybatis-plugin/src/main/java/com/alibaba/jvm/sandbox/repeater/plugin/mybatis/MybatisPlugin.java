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
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterConfig;
import com.alibaba.jvm.sandbox.repeater.plugin.spi.InvokePlugin;

import com.google.common.collect.Lists;
import org.kohsuke.MetaInfServices;

/**
 * <p>
 *
 * @author zhaoyb1990
 */
@MetaInfServices(InvokePlugin.class)
public class MybatisPlugin extends AbstractInvokePluginAdapter {

    private volatile  EventListener sharedEventListner = null;

    @Override
    protected List<EnhanceModel> getEnhanceModels() {
        EnhanceModel em = EnhanceModel.builder()
                .classPattern("org.apache.ibatis.binding.MapperMethod")
                .methodPatterns(EnhanceModel.MethodPattern.transform("execute"))
                .watchTypes(Type.BEFORE, Type.RETURN, Type.THROWS)
                .build();
        EnhanceModel em2 = EnhanceModel.builder()
            .classPattern("org.apache.ibatis.session.defaults.DefaultSqlSession")
            .methodPatterns(EnhanceModel.MethodPattern.transform("selectOne","selectMap","selectCursor","selectList","insert","update","delete"))
            .watchTypes(Type.BEFORE, Type.RETURN, Type.THROWS)
            .build();

        return Lists.newArrayList(em,em2);
    }

    @Override
    protected InvocationProcessor getInvocationProcessor() {
        return new MybatisProcessor(getType());
    }

    @Override
    public InvokeType getType() {
        return InvokeType.MYBATIS;
    }

    @Override
    public String identity() {
        return "mybatis";
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
