package com.alibaba.jvm.sandbox.repeater.plugin.dubbo;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.repeater.plugin.Constants;
import com.alibaba.jvm.sandbox.repeater.plugin.api.InvocationListener;
import com.alibaba.jvm.sandbox.repeater.plugin.api.InvocationProcessor;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultEventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.TraceFactory;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.LogUtil;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.ArsmHeader;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.DubboInvocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.util.List;
import java.util.Map;


/**
 * {@link DubboEventListener}
 * <p>
 *
 * @author zhaoyb1990
 */
public class DubboEventListener extends DefaultEventListener {

    public DubboEventListener(InvokeType invokeType, boolean entrance, InvocationListener listener, InvocationProcessor processor) {
        super(invokeType, entrance, listener, processor);
    }


    @Override
    protected void initContext(Event event) {
        if (entrance && isEntranceBegin(event)) {
            if(DubboRunTimeUtil.isAliDubbo()){
                String traceId = getTraceId();
                TraceFactory.start(traceId);
                return;
            }
            TraceFactory.start();
        }
    }

    private String getTraceId() {
        String traceId = RpcContext.getContext().getAttachment(Constants.REPEAT_TRACE_ID);
        if(null == traceId){
            traceId = RpcContext.getContext().getAttachment(ArsmHeader.EAGLEEYE_TRACE_ID.getName());
        }
        return traceId;
    }

    @Override
    protected Invocation initInvocation(BeforeEvent event) {
        DubboInvocation dubboInvocation = new DubboInvocation();
        dubboInvocation.setAliDubbo(DubboRunTimeUtil.isAliDubbo());
        // onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {}
        Object invoker = null;
        Object invocation = null;
        if(DubboRunTimeUtil.isAliDubbo()){
            invoker = event.argumentArray[0];
            invocation = event.argumentArray[1];
        }else{
            invoker = event.argumentArray[1];
            invocation = event.argumentArray[2];
        }

        try {
            Object url = MethodUtils.invokeMethod(invoker, "getUrl");
            Map<String, String> parameters = (Map<String, String>) MethodUtils.invokeMethod(url, "getParameters");
            String port = String.valueOf(MethodUtils.invokeMethod(url, "getPort"));
            String host = (String)MethodUtils.invokeMethod(url, "getHost");
            String protocol =  (String)MethodUtils.invokeMethod(url, "getProtocol");
            // methodName
            String methodName = (String) MethodUtils.invokeMethod(invocation, "getMethodName");
            Class<?>[] parameterTypes = (Class<?>[]) MethodUtils.invokeMethod(invocation, "getParameterTypes");

            //attachments
            Map<String, String> attachments = (Map<String, String>) MethodUtils.invokeMethod(invocation, "getAttachments");

            // interfaceName
            String interfaceName = ((Class) MethodUtils.invokeMethod(invoker, "getInterface")).getCanonicalName();

            dubboInvocation.setProtocol(protocol);
            dubboInvocation.setInterfaceName(interfaceName);
            dubboInvocation.setMethodName(methodName);
            dubboInvocation.setParameters(parameters);
            dubboInvocation.setVersion(parameters.get("version"));
            dubboInvocation.setGroup(parameters.get("group"));
            dubboInvocation.setParameterTypes(transformClass(parameterTypes));
            dubboInvocation.setAddress(host);
            dubboInvocation.setPort(port);
            dubboInvocation.setAttachments(attachments);
            // todo find a right way to get address and group
        } catch (Exception e) {
            LogUtil.error("error occurred when init dubbo invocation", e);
        }
        return dubboInvocation;
    }


    private String[] transformClass(Class<?>[] parameterTypes) {
        List<String> paramTypes = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(parameterTypes)) {
            for (Class<?> clazz : parameterTypes) {
                paramTypes.add(clazz.getCanonicalName());
            }
        }
        return paramTypes.toArray(new String[0]);
    }


}
