package com.alibaba.jvm.sandbox.repeater.plugin.mybatis.identity;

import java.lang.reflect.Field;
import java.util.HashMap;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Identity;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

/**
 * @author zhuangpeng
 * @since 2020/11/5
 */
public class MapperMethodIdentityFactory implements MybatisIdentityFactory {
    @Override
    public Identity getIdentity(BeforeEvent event) {
        Object mapperMethod = event.target;
        Identity identity = null;
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
        return identity;
    }
}
