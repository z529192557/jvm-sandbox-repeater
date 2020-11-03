package com.alibaba.jvm.sandbox.repeater.plugin.domain;

/**
 * Arsm相关常量
 *
 * @author zhuangpeng
 * @since 2020/11/3
 */
public enum ArsmHeader {

        EAGLEEYE_TRACE_ID("EagleEye-TraceID"),
        EAGLEEYE_RPC_ID("EagleEye-RpcID"),
        EAGLEEYE_IP("EagleEye-IP"),
        EAGLEEYE_ROOT_APP("EagleEye-ROOT-APP"),
        SPAN_ID("EagleEye-SpanID"),
        PARENT_SPAN_ID("EagleEye-pSpanID"),
        SAMPLED("EagleEye-Sampled"),
        PARENT_APPLICATION_NAME("EagleEye-pAppName"),
        PARENT_RPC_NAME("EagleEye-pRpc"),
        EAGLEEYE_USERDATA("EagleEye-UserData");

        private String name;

        private ArsmHeader(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

    public String getName() {
        return name;
    }
}
