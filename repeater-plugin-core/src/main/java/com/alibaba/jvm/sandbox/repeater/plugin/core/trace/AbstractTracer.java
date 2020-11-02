package com.alibaba.jvm.sandbox.repeater.plugin.core.trace;

import com.alibaba.jvm.sandbox.repeater.plugin.api.Tracer;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.ApplicationModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.TraceContext;
import com.alibaba.ttl.TransmittableThreadLocal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuangpeng
 * @since 2020/9/29
 */
public abstract class AbstractTracer implements Tracer {

    private final static Logger log = LoggerFactory.getLogger(AbstractTracer.class);

    private static ThreadLocal<TraceContext> ttlContext = new TransmittableThreadLocal<TraceContext>();

    private static ThreadLocal<TraceContext> normalContext = new ThreadLocal<TraceContext>();

    @Override
    public TraceContext start() {
        return start(null);
    }

    @Override
    public TraceContext start(String traceId) {
        TraceContext context = DefaultTracer.getContextCarrie().get();
        if (context != null) {
            return context;
        }

        if (!isValid(traceId)) {
            traceId = generate();
        }
        context = new TraceContext(traceId,this);
        if(log.isDebugEnabled()){
            log.debug("[Tracer] start trace success,traceId={},timestamp={}", context.getTraceId(), context.getTimestamp());
        }
        AbstractTracer.getContextCarrie().set(context);
        return context;
    }

    @Override
    public TraceContext getContext() {
        return AbstractTracer.getContextCarrie().get();
    }

    @Override
    public String getTraceId() {
        TraceContext traceContext = getContext();
        if(null != traceContext){
            return traceContext.getTraceId();
        }
        return null;
    }

    @Override
    public void end() {
        final TraceContext context = getContext();
        if (context != null && log.isDebugEnabled()) {
            log.debug("[Tracer] stop  trace success,type={},traceId={},cost={}ms", context.getInvokeType(), context.getTraceId(), System.currentTimeMillis() - context.getTimestamp());
        }
        getContextCarrie().remove();
    }

    /**
     * 获取计算采样率的种子
     */
    public abstract Long getSampleBit(String traceId);

    /**
     * 生成traceId
     */
    public abstract String generate();

    /**
     * 验证traceId
     */
    public abstract boolean isValid(String traceId);
    /**
     * 根据用户是否开启ttl选择合适的载体
     *
     * @return 上下文threadLocal载体
     * @see com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterConfig#setUseTtl(boolean)
     */
    protected static ThreadLocal<TraceContext> getContextCarrie() {
        return ApplicationModel.instance().getConfig().isUseTtl() ? ttlContext : normalContext;
    }
}
