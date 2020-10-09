package com.alibaba.jvm.sandbox.repeater.plugin.mybatis;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.InvokeEvent;
import com.alibaba.jvm.sandbox.api.event.ReturnEvent;
import com.alibaba.jvm.sandbox.api.event.ThrowsEvent;
import com.alibaba.jvm.sandbox.repeater.plugin.core.cache.RepeatCache;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultInvocationProcessor;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.TraceFactory;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Identity;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;

import javafx.scene.chart.XYChart.Data;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.ws.handler.HandlerResolver;

/**
 * <p>
 *
 * @author zhaoyb1990
 */
class MybatisProcessor extends DefaultInvocationProcessor {

    MybatisProcessor(InvokeType type) {
        super(type);
    }

    @Override
    public Identity assembleIdentity(BeforeEvent event) {
        Object mapperMethod = event.target;
        // SqlCommand = MapperMethod.command
        Identity identity = null;
        if(event.javaClassName.equals("org.apache.ibatis.binding.MapperMethod")){
            Field field = FieldUtils.getDeclaredField(mapperMethod.getClass(), "command", true);
            if (field == null) {
                return new Identity(InvokeType.MYBATIS.name(), "Unknown", "Unknown", new HashMap<String, String>(1));
            }
            try {
                Object command = field.get(mapperMethod);
                Object name = MethodUtils.invokeMethod(command, "getName");
                Object type = MethodUtils.invokeMethod(command, "getType");
                identity =  new Identity(InvokeType.MYBATIS.name(), type.toString(), name.toString(), new HashMap<String, String>(1));
            } catch (Exception e) {
                identity = new Identity(InvokeType.MYBATIS.name(), "Unknown", "Unknown", new HashMap<String, String>(1));
            }
        }else if(event.javaClassName.equals("org.apache.ibatis.session.defaults.DefaultSqlSession")){
            try {
                Object name = event.argumentArray[0];
                Object type = getIdentityType(event.javaMethodName);
                identity =  new Identity(InvokeType.MYBATIS.name(), type.toString(), name.toString(), new HashMap<String, String>(1));
            } catch (Exception e) {
                identity = new Identity(InvokeType.MYBATIS.name(), "Unknown", "Unknown", new HashMap<String, String>(1));
            }
        }
        return identity;
    }

    private Object getIdentityType(String javaMethodName) {
        return SqlSessionMethodConfiguration.getSqlCommmandType(javaMethodName);
    }

    @Override
    public Object[] assembleRequest(BeforeEvent event) {
        if(event.javaClassName.equals("org.apache.ibatis.binding.MapperMethod")){
            return new Object[]{event.argumentArray[1]};
        }else{
            return event.argumentArray;
        }
    }

    @Override
    public boolean inTimeSerializeRequest(Invocation invocation, BeforeEvent event) {
        return false;
    }
}
