package com.alibaba.jvm.sandbox.repeater.plugin.api;

import com.alibaba.jvm.sandbox.repeater.plugin.domain.TraceContext;

/**
 * @author zhuangpeng
 * @since 2020/9/29
 */
public interface Tracer {

    /**
     * 开启追踪一次调用，非线程安全
     *
     * @return 调用上下文
     */
    TraceContext start();

    /**
     * 开启追踪一次调用
     *
     * @param traceId 调用唯一
     * @return 调用上下文
     */
    TraceContext start(String traceId);

    /**
     * 验证traceId
     */
    boolean isValid(String traceId);

    /**
     * 获取当前上下文
     *
     * @return TraceContext
     */
    TraceContext getContext();

    /**
     * 获取当前上下文的追踪ID，未开启追踪情况下返回空
     *
     * @return 调用追踪ID
     */
    String getTraceId();

    /**
     * 结束追踪一次调用，清理上下文
     */
    void end();

    /**
     * 获取计算采样率的种子
     */
    Long getSampleBit(String traceId);

    /**
     *  生成traceId
     */
    String generate();
}
