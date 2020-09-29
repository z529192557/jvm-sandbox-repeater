package com.alibaba.jvm.sandbox.repeater.plugin.core.trace;

import com.alibaba.jvm.sandbox.repeater.plugin.api.Tracer;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.ApplicationModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterConfig;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.TraceContext;
import com.alibaba.ttl.TransmittableThreadLocal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DefaultTracer} 默认的用内部全局跟踪能力
 * <p>
 * 非常核心的能力；全局的信息需要利用它来串联
 * <p>
 * 如果不开启{@link RepeaterConfig#setUseTtl(boolean)}，只能录制到单线程的子调用信息
 * <p>
 * 由于上下文信息是从entrance插件开启{@code Tracer.start()}，必须在entrance插件进行关闭({@code Tracer.end()})，否则会出现上下文错乱问题
 * </p>
 *
 * @author zhaoyb1990
 * @since 1.0.0
 */
public class DefaultTracer extends AbstractTracer{

    private final static Logger log = LoggerFactory.getLogger(DefaultTracer.class);

    private final static DefaultTraceGenerator traceGenerator = new DefaultTraceGenerator();

    @Override
    public boolean isValid(String traceId) {
        return traceGenerator.isValid(traceId);
    }

    @Override
    public Long getSampleBit(String traceId) {
        return this.isValid(traceId) ? Long.parseLong(traceId.substring(25, 30)) : 9999L;
    }

    @Override
    public String generate() {
        return traceGenerator.generate();
    }
}
