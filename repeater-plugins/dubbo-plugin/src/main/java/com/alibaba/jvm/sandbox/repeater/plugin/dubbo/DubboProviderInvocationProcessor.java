package com.alibaba.jvm.sandbox.repeater.plugin.dubbo;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.Event.Type;
import com.alibaba.jvm.sandbox.api.event.InvokeEvent;
import com.alibaba.jvm.sandbox.repeater.plugin.core.cache.RepeatCache;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.ApplicationModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.util.CollectionUtils;

/**
 * {@link DubboProviderInvocationProcessor} dubbo服务端调用处理
 * <p>
 *
 * @author zhaoyb1990
 */
class DubboProviderInvocationProcessor extends DubboConsumerInvocationProcessor {

    DubboProviderInvocationProcessor(InvokeType type) {
        super(type);
    }


    @Override
    public boolean ignoreEvent(InvokeEvent event) {
        if(event.type == Type.BEFORE){
            BeforeEvent beforeEvent = (BeforeEvent)event;
            Object invoker = null;
            Object invocation = null;
            if(DubboRunTimeUtil.isAliDubbo()){
                invoker = beforeEvent.argumentArray[0];
                invocation = beforeEvent.argumentArray[1];
            }else{
                invoker = beforeEvent.argumentArray[1];
                invocation = beforeEvent.argumentArray[2];
            }

            try {
                String methodName = (String) MethodUtils.invokeMethod(invocation, "getMethodName");
                String interfaceName = ((Class) MethodUtils.invokeMethod(invoker, "getInterface")).getCanonicalName();

                if(ApplicationModel.instance().getConfig().dubboWhiteInterfaceMode){
                    if(!match(interfaceName,methodName,ApplicationModel.instance().getConfig().getDubboEntrancePatterns())){
                        return true;
                    }
                }

            } catch (Exception e) {

            }

        }
        return false;
    }

    private boolean match(String interfaceName, String methodName, List<String> dubboEntrancePatterns) {
        if(null == dubboEntrancePatterns || dubboEntrancePatterns.size() == 0){
            return false;
        }
        String classMethod = interfaceName + "#" + methodName;

        if(dubboEntrancePatterns.contains(classMethod)){
            return true;
        }

       Iterator<String> iterator =  dubboEntrancePatterns.iterator();
       while (iterator.hasNext()){
           String pattern = iterator.next();
           if(classMethod.contains(pattern)){
               return true;
           }
       }

       return false;
    }
}
