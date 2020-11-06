package com.alibaba.jvm.sandbox.repeater.config.trace;
import java.util.UUID;

import com.alibaba.arms.tracing.Tracer;
import com.alibaba.jvm.sandbox.repeater.plugin.Constants;
import com.alibaba.jvm.sandbox.repeater.plugin.annotation.ConfigActive;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.AbstractTracer;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.TraceContext;
import com.alibaba.ttl.TransmittableThreadLocal;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.MetaInfServices;

/**
 * @author zhuangpeng
 * @since 2020/9/21
 */
@ConfigActive(value = Constants.ARMS)
@MetaInfServices(com.alibaba.jvm.sandbox.repeater.plugin.api.Tracer.class)
public class ArmsTrace extends AbstractTracer {

    private static ThreadLocal<TraceContext> ttlContext = new TransmittableThreadLocal<TraceContext>();

    private static ThreadLocal<TraceContext> normalContext = new ThreadLocal<TraceContext>();

    @Override
    public Long getSampleBit(String traceId) {
        return this.isValid(traceId) ? traceId.hashCode() : 9999L;
    }

    public String generate() {
        String traceId = Tracer.builder().getSpan().getTraceId();
        if(StringUtils.isBlank(traceId)){
            traceId = UUID.randomUUID().toString().replace("-","");
        }
        return traceId;
    }

    public boolean isValid(String traceId) {
        if (StringUtils.isBlank(traceId)) {
            return false;
        }
        return true;
    }
}
