package com.alibaba.jvm.sandbox.repeater.plugin.mybatis.identity;

import java.util.HashMap;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Identity;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;
import com.alibaba.jvm.sandbox.repeater.plugin.mybatis.SqlSessionMethodConfiguration;

/**
 * @author zhuangpeng
 * @since 2020/11/5
 */
public class SqlSessionIdentityFactory implements MybatisIdentityFactory{
    @Override
    public Identity getIdentity(BeforeEvent event) {
        Identity identity = null;
        try {
            Object name = event.argumentArray[0];
            Object type = getIdentityType(event.javaMethodName);
            identity =  new Identity(InvokeType.MYBATIS.name(), type.toString(), name.toString(), new HashMap<String, String>(1));
        } catch (Exception e) {
            identity = new Identity(InvokeType.MYBATIS.name(), "Unknown", "Unknown", new HashMap<String, String>(1));
        }
        return identity;
    }
    private Object getIdentityType(String javaMethodName) {
        return SqlSessionMethodConfiguration.getSqlCommmandType(javaMethodName);
    }
}
