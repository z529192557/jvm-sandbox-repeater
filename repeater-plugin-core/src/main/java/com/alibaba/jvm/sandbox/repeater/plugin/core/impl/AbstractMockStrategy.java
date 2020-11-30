package com.alibaba.jvm.sandbox.repeater.plugin.core.impl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alibaba.jvm.sandbox.repeater.plugin.core.cache.RepeatCache;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.Serializer.Type;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializerProvider;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.SequenceGenerator;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MockInvocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.mock.MockRequest;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.mock.MockResponse;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.mock.MockResponse.Action;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.mock.SelectResult;
import com.alibaba.jvm.sandbox.repeater.plugin.exception.RepeatException;
import com.alibaba.jvm.sandbox.repeater.plugin.spi.MockStrategy;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractMockStrategy}抽象的mock策略执行；子类只需要实现{@code select}方法即可
 * <p>
 *
 * @author zhaoyb1990
 */
public abstract class AbstractMockStrategy implements MockStrategy {

    protected final static Logger log = LoggerFactory.getLogger(AbstractMockStrategy.class);

    /**
     * 选择出回放的invocation
     *
     * @param request mock回放请求
     * @return 选择结果
     */
    protected abstract SelectResult doSelect(final MockRequest request);


    protected SelectResult select(MockRequest request){

        SelectResult excepiton = mockExcepiton(request);
        if(null != excepiton){
            return excepiton;
        }
        return doSelect(request);
    }

    protected SelectResult mockExcepiton(MockRequest request){
        if(request.hasMockExceptionConfig() && StringUtils.isNotBlank(request.getMockExceptionIndentities())){
            String mockExceptionIndentities = request.getMockExceptionIndentities();
            List<String> mockIdndentiyList = Lists.newArrayList();
            if (StringUtils.isNotEmpty(mockExceptionIndentities)){
                mockIdndentiyList.addAll(Arrays.asList(mockExceptionIndentities.split("\\|")));
            }
            if (mockIdndentiyList.contains(request.getIdentity().getUri())) {
                return SelectResult.builder().match(true).cost(0L).mockExcepiton().build();
            }
        }
        return null;
    }

    @Override
    public MockResponse execute(final MockRequest request) {
        MockResponse response;
        try {
            /*
             * before select hook;
             */
            MockInterceptorFacade.instance().beforeSelect(request);

            /*
             * do select
             */
            SelectResult select = select(request);
            Invocation invocation = select.getInvocation();
            MockInvocation mi = new MockInvocation();
            mi.setIndex(SequenceGenerator.generate(request.getTraceId() + "#"));
            mi.setCurrentUri(request.getIdentity().getUri());
            mi.setCurrentArgs(request.getArgumentArray());
            //要对curentArgs进行立即序列化
            mi.setCurrentArgsSerialized(SerializerProvider.instance().provide(Type.HESSIAN).serialize2String(request.getArgumentArray()));
            mi.setTraceId(request.getTraceId());
            mi.setCost(select.getCost());
            mi.setRepeatId(request.getRepeatId());
            // add mock invocation
            RepeatCache.addMockInvocation(mi);
            // matching success
            if (select.isMatch() && invocation != null) {
                response = MockResponse.builder()
                        .action(invocation.getThrowable() == null ? Action.RETURN_IMMEDIATELY : Action.THROWS_IMMEDIATELY)
                        .throwable(invocation.getThrowable())
                        .invocation(invocation)
                        .build();
                mi.setSuccess(true);
                mi.setOriginUri(invocation.getIdentity().getUri());
                mi.setOriginArgs(invocation.getRequest());
            }else if(select.isExceptionMock()){
                response = MockResponse.builder()
                    .action(Action.THROWS_IMMEDIATELY)
                    .throwable(new RepeatException("mock excepiton occur")).build();
            }else {
                response = MockResponse.builder()
                        .action(Action.THROWS_IMMEDIATELY)
                        .throwable(new RepeatException("no matching invocation found")).build();
            }
            /*
             * before return hook;
             */
            MockInterceptorFacade.instance().beforeReturn(request, response);
        } catch (Throwable throwable) {
            log.error("[Error-0000]-uncaught exception occurred when execute mock strategy, type={}", type(), throwable);
            response = MockResponse.builder().
                    action(Action.THROWS_IMMEDIATELY)
                    .throwable(throwable)
                    .build();
        }
        return response;
    }
}
