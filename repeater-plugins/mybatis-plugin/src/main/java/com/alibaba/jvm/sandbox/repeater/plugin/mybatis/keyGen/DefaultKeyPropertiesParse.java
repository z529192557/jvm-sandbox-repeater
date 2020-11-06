package com.alibaba.jvm.sandbox.repeater.plugin.mybatis.keyGen;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MybatisInvocation;
import com.alibaba.jvm.sandbox.repeater.plugin.mybatis.MybatisPlugin;

/**
 * @author zhuangpeng
 * @since 2020/11/5
 */
public class DefaultKeyPropertiesParse implements KeyPropertiesParse{

    private static DefaultKeyPropertiesParse DEFAULTF= new DefaultKeyPropertiesParse();

    public Map<String, KeyPropertiesParse> key2KeyPropertiesParseMap = new HashMap<>();

    private DefaultKeyPropertiesParse(){
        key2KeyPropertiesParseMap.put(MybatisPlugin.MAPPERMETHOD,new MapperMethodPropertiesParse());
        key2KeyPropertiesParseMap.put(MybatisPlugin.DEFAULTSQLSESSION,new SqlSessionInsertKeyPropertiesParse());
    }


    public static KeyPropertiesParse getInstance(){
        return DEFAULTF;
    }

    @Override
    public void parse(MybatisInvocation invocation, String[] keyPropertise) throws SerializeException {
         key2KeyPropertiesParseMap.get(invocation.hookClass).parse(invocation,keyPropertise);
    }

    @Override
    public void mockKeyGenerator(Object[] currentParam,MybatisInvocation invocation) {
        key2KeyPropertiesParseMap.get(invocation.hookClass).mockKeyGenerator(currentParam,invocation);
    }
}
