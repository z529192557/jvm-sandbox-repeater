package com.alibaba.jvm.sandbox.repeater.plugin.core.impl.mock;

import java.util.List;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.repeater.plugin.core.cache.RepeatCache;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultEventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.Serializer.Type;
import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializerProvider;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.SequenceGenerator;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Identity;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MockConfig;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MockInvocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MockType;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeatContext;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuangpeng
 * @since 2020/11/30
 */
public class DefaultMockFilterHandler implements MockFilterHandler{

    protected static Logger log = LoggerFactory.getLogger(DefaultEventListener.class);

    @Override
    public boolean needMock(Object[] argumentArray, Identity identity, RepeatContext context) {
        if(null != context && null != context.getMeta() && null != context.getMeta().getReplayStrategy() && null != context.getMeta().getReplayStrategy().getMockConfig()){
            try {
                MockConfig mockConfig = context.getMeta().getReplayStrategy().getMockConfig();
                MockType mockType = mockConfig.getMockType();
                if(!doNeedMock(mockType,identity,mockConfig)){
                    MockInvocation mi = new MockInvocation();
                    mi.setIndex(SequenceGenerator.generate(context.getTraceId()));
                    mi.setCurrentUri(identity.getUri());
                    mi.setCurrentArgs(argumentArray);
                    mi.setSkip(true);
                    mi.setSkipMsg("mock filter decide not mock");
                    //要对curentArgs进行立即序列化
                    mi.setCurrentArgsSerialized(SerializerProvider.instance().provide(Type.HESSIAN).serialize2String(mi.getCurrentArgs()));
                    mi.setTraceId(context.getTraceId());
                    mi.setRepeatId(context.getMeta().getRepeatId());
                    // add mock invocation
                    RepeatCache.addMockInvocation(mi);
                    return false;
                }
            } catch (SerializeException e) {
                log.info("SerializeException",e);
            }
        }
        return true;
    }

    private boolean doNeedMock(MockType mockType, Identity identity, MockConfig mockConfig) {
        if (CollectionUtils.isEmpty(mockConfig.getIdentities())) {
            return true;
        }

        List<String> identities = mockConfig.getIdentities();

        boolean matchIdentity = false;
        for (String expression : identities) {
            if (match(identity, expression)) {
                matchIdentity =  true;
            }
        }
        boolean mock = true;
        if (mockType == MockType.MOCK_PART) {
            // 如果是只mock部分，则配置的子调用信息如果匹配，则mock，否则不mock
            mock = matchIdentity;
        } else if (mockType == MockType.NOT_MOCK_PART) {
            // 如果是不mock部分，则配置的子调用信息如果匹配，则不mock，否则mock
            mock = !matchIdentity;
        }

        return mock;
    }

    private boolean match(Identity identity, String expression) {
        String id = identity.getUri();
        if (id.equals(expression)) {
            return true;
        }
        return id.matches(expression);
    }

}
