package com.alibaba.jvm.sandbox.repeater.plugin.mybatis;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.Event.Type;
import com.alibaba.jvm.sandbox.api.event.ReturnEvent;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.AbstraceEventListener;

/**
 * @author zhuangpeng
 * @since 2020/11/5
 */
public class MybatisConfigurationEventListener extends AbstraceEventListener {

    //将mybatis的 MappedStatement放入线程上下文
    @Override
    public void onEvent(Event event){
        if(sample(event) && event.type == Type.RETURN){
            ReturnEvent returnEvent = (ReturnEvent)event;
            MybatisInvocationProcessor.setMappedStatement(returnEvent.object);
        }
    }
}
