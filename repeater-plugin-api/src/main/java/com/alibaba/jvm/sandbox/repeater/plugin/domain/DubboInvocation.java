package com.alibaba.jvm.sandbox.repeater.plugin.domain;

import java.util.Map;

/**
 * {@link DubboInvocation}
 * <p>
 * dubbo的调用，自定义属性
 *
 * @author zhaoyb1990
 */
public class DubboInvocation extends Invocation {

    private String protocol;
    private String version;
    private String address;
    private String port;
    private String group;
    private String interfaceName;
    private String methodName;
    private String[] parameterTypes;
    private Map<String,String> parameters;
    private Map<String,String> attachments;
    private boolean isAliDubbo;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }

    public boolean isAliDubbo() {
        return isAliDubbo;
    }

    public void setAliDubbo(boolean aliDubbo) {
        isAliDubbo = aliDubbo;
    }
}
