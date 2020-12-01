package com.alibaba.jvm.sandbox.repeater.plugin.dubbo;

/**
 * 多环境下会发生变化的dubbo元数据配置，需要从本机获取
 *
 * @author zhuangpeng
 * @since 2020/9/27
 */
public class DubboMetaConfig {

    /**
     * 分组
     */
    private String group;
    /**
     * 版本号
     */
    private String version;
    /**
     * 端口
     */
    private String port;

    /**
     * 超时配置
     */
    private Integer timeOut;

    public String getGroup() {
        return group;
    }

    public String getVersion() {
        return version;
    }

    public String getPort() {
        return port;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public static DubboMetaConfigBuilder  getBuilder(){
        return new DubboMetaConfigBuilder();
    }

    public static class DubboMetaConfigBuilder {

        /**
         * 分组
         */
        private String group;
        /**
         * 版本号
         */
        private String version;
        /**
         * 端口
         */
        private String port;

        /**
         * 超时配置
         */
        private Integer timeOut = 30000;

        public DubboMetaConfig build(){
            DubboMetaConfig config = new DubboMetaConfig();
            config.group = group;
            config.version = version;
            config.port = port;
            config.timeOut = timeOut;
            return config;
        }

        public DubboMetaConfigBuilder setGroup(String group){
            if(null != group){
                this.group = group;
            }
            return this;
        }

        public DubboMetaConfigBuilder setVersion(String version){
            if(null != version){
                this.version = version;
            }
            return this;
        }

        public DubboMetaConfigBuilder setPort(String port){
            if(null != port){
                this.port = port;
            }
            return this;
        }

        public DubboMetaConfigBuilder setTimeOut(Integer timeOut){
            if(null != timeOut){
                this.timeOut = timeOut;
            }
            return this;
        }

        public String getGroup() {
            return group;
        }

        public String getVersion() {
            return version;
        }

        public String getPort() {
            return port;
        }

        public boolean hasNull() {
            return null == group || null == version || null == port;
        }
    }
}
