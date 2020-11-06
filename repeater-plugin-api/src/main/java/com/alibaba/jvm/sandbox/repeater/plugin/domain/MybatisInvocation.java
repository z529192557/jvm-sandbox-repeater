package com.alibaba.jvm.sandbox.repeater.plugin.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Invocation} 描述一次mybatis调用
 */
public class MybatisInvocation extends Invocation {

    /**
     *  是否使用keyGenerator
     */
    public String keyGenerator;

    /**
     * key填充的属性
     */
    public Map<String,Object> keyProperties2Value = new HashMap<>(8);

    /**
     * hookmethod
     */
    public String hookClass;

    /**
     * 请求参数 - snapshot 不做传输使用，传输需要序列化值
     *
     * @see MybatisInvocation#requestFinshSerialized
     */
    private transient Object[] requestFinsh;

    /**
     * 序列化之后的请求值，录制时候作为{@link MybatisInvocation#requestFinsh}的载体传输；回放时候需要还原成{@link MybatisInvocation#requestFinsh}
     */
    private String requestFinshSerialized;

    public String getHookClass() {
        return hookClass;
    }

    public void setHookClass(String hookClass) {
        this.hookClass = hookClass;
    }

    public boolean isInsert(){
       return "INSERT".equals(this.getIdentity().getLocation());
    }

    public String getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(String keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public Map<String, Object> getKeyProperties2Value() {
        return keyProperties2Value;
    }

    public void setKeyProperties2Value(Map<String, Object> keyProperties2Value) {
        this.keyProperties2Value = keyProperties2Value;
    }

    public Object[] getRequestFinsh() {
        return requestFinsh;
    }

    public void setRequestFinsh(Object[] requestFinsh) {
        this.requestFinsh = requestFinsh;
    }

    public String getRequestFinshSerialized() {
        return requestFinshSerialized;
    }

    public void setRequestFinshSerialized(String requestFinshSerialized) {
        this.requestFinshSerialized = requestFinshSerialized;
    }
}