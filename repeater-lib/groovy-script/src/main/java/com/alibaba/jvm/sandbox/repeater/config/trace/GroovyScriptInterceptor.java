package com.alibaba.jvm.sandbox.repeater.config.trace;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import com.alibaba.jvm.sandbox.repeater.plugin.core.cache.RepeatCache;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MockInvocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.mock.MockRequest;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.mock.MockResponse;
import com.alibaba.jvm.sandbox.repeater.plugin.spi.MockInterceptor;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Groovy脚本拦截器
 *
 * @author zhuangpeng
 * @since 2020/9/21
 */
@MetaInfServices(com.alibaba.jvm.sandbox.repeater.plugin.spi.MockInterceptor.class)
public class GroovyScriptInterceptor implements MockInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyScriptInterceptor.class);

    private Map<String, Script> scriptCache = new ConcurrentHashMap<String, Script>();

    @Override
    public boolean matchingSelect(MockRequest request) {
        return null != request.getGroovyScript();
    }


    @Override
    public void beforeSelect(MockRequest request) {
        String groovyScript = request.getGroovyScript();
        //从录制的子调用中获取所有identify一直的子调用，统一经过groovy处理
        Iterable<Invocation> typeEqualsInvocations = Iterables.filter( request.getRecordModel().getSubInvocations(), new Predicate<Invocation>() {
            @Override
            public boolean apply(Invocation input) {
                return input.getIdentity().getUri().equalsIgnoreCase(request.getIdentity().getUri());
            }
        });

        for (Invocation mockValue : typeEqualsInvocations) {

            Object[] originRequest = mockValue.getRequest();
            Object[] currentRequest = request.getArgumentArray();
            Binding binding = new Binding();

            binding.setVariable("originRequest", originRequest);
            binding.setVariable("currentRequest", currentRequest);
            binding.setVariable("context", request);

            // Groovy表达式级别缓存脚本，避免持续重复编译导致的OOM
            CRC32 crc32 = new CRC32();
            crc32.update(groovyScript.getBytes());
            String cacheKey = String.valueOf(crc32.getValue());
            Script script = scriptCache.get(cacheKey);
            try {
                if (script == null) {
                    script = new GroovyShell().parse(groovyScript);
                    scriptCache.put(cacheKey, script);
                }
                // 防止binding出现线程安全问题
                InvokerHelper.createScript(script.getClass(), binding).run();
            } catch (Throwable t) {
                LOGGER.error("groovy script interceptor with exception", t);
                MockInvocation mockInvocation = new MockInvocation();
                mockInvocation.setTraceId(request.getTraceId());
                mockInvocation.setCurrentUri(request.getIdentity().getUri());
                mockInvocation.setCurrentArgs(request.getArgumentArray());
                mockInvocation.setSuccess(false);
                mockInvocation.setCurrentArgs(new Object[]{"pre-mock groovy script hook with exception"});
                RepeatCache.addMockInvocation(mockInvocation);
                throw new RuntimeException("execute pre-hook groovy script [" + script + "] with exception {}", t);
            }
        }

    }

    @Override
    public boolean matchingReturn(MockRequest request, MockResponse response) {
        return false;
    }

    @Override
    public void beforeReturn(MockRequest request, MockResponse response) {

    }
}
