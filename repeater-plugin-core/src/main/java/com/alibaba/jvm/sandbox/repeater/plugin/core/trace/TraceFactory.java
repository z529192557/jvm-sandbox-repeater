package com.alibaba.jvm.sandbox.repeater.plugin.core.trace;

import com.alibaba.jvm.sandbox.repeater.plugin.api.Tracer;
import com.alibaba.jvm.sandbox.repeater.plugin.core.config.BootStrapConfigFacotry;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.ApplicationModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.TraceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuangpeng
 * @since 2020/9/29
 */
public class TraceFactory {

    private final static Logger log = LoggerFactory.getLogger(TraceFactory.class);

    public static volatile Tracer tracerImpl = null;

    static {
        initTraceFactory();
    }

    private static void initTraceFactory() {
        tracerImpl = BootStrapConfigFacotry.getInstance().getTraceGenerator(ApplicationModel.instance().getConfig().getTraceName());
        log.info("find trace implementation : {}", tracerImpl.getClass().getCanonicalName());
    }

    /**
     * 开启追踪一次调用，非线程安全
     *
     * @return 调用上下文
     */
    public static TraceContext start() {
        return start(null);
    }

    /**
     * 开启追踪一次调用，非线程安全
     *
     * @param traceId 调用唯一
     * @return 调用上下文
     */
    public static TraceContext start(String traceId) {
        return tracerImpl.start(traceId);
    }

    /**
     * 获取当前上下文
     *
     * @return TraceContext
     */
    public static TraceContext getContext() {
        return tracerImpl.getContext();
    }

    /**
     * 获取当前上下文的追踪ID，未开启追踪情况下返回空
     *
     * @return 调用追踪ID
     */
    public static String getTraceId() {
        return tracerImpl.getTraceId();
    }

    /**
     * 结束追踪一次调用，清理上下文
     */
    public static void end() {
        tracerImpl.end();
    }

    /**
     * 验证traceId
     */
    public static boolean isValid(String traceId){
        return tracerImpl.isValid(traceId);
    }

    public static String generate() {
        return tracerImpl.generate();
    }
}
