package com.alibaba.jvm.sandbox.repeater.plugin.core.impl.mock;

import java.util.List;

import com.alibaba.jvm.sandbox.repeater.plugin.domain.Identity;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MockConfig;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.MockType;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeatContext;

import org.apache.commons.collections4.CollectionUtils;

/**
 * @author zhuangpeng
 * @since 2020/11/30
 */
public class DefaultMockFilterHandler implements MockFilterHandler{
    @Override
    public boolean needMock(Identity identity, RepeatContext context) {
        if(null != context && null != context.getMeta() && null != context.getMeta().getReplayStrategy() && null != context.getMeta().getReplayStrategy().getMockConfig()){
            MockConfig mockConfig = context.getMeta().getReplayStrategy().getMockConfig();
            MockType mockType = mockConfig.getMockType();
            return doNeedMock(mockType,identity,mockConfig);
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
