package com.alibaba.jvm.sandbox.repeater.plugin.mybatis.identity;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Identity;
import com.alibaba.jvm.sandbox.repeater.plugin.mybatis.MybatisPlugin;

/**
 * @author zhuangpeng
 * @since 2020/11/5
 */
public class DefaultMybatisIdentityFactory implements MybatisIdentityFactory{

    private static DefaultMybatisIdentityFactory DEFAULTFACTORY = new DefaultMybatisIdentityFactory();

    public Map<String,MybatisIdentityFactory> key2MybatisIdentityFactory = new HashMap<>();

    private DefaultMybatisIdentityFactory(){
        key2MybatisIdentityFactory.put(MybatisPlugin.MAPPERMETHOD,new MapperMethodIdentityFactory());
        key2MybatisIdentityFactory.put(MybatisPlugin.DEFAULTSQLSESSION,new SqlSessionIdentityFactory());
    }

    @Override
    public Identity getIdentity(BeforeEvent event) {
        return key2MybatisIdentityFactory.get(event.javaClassName).getIdentity(event);
    }

    public static MybatisIdentityFactory getInstance(){
        return DEFAULTFACTORY;
    }
}
