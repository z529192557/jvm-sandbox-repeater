package com.alibaba.jvm.sandbox.repeater.plugin.core.impl.mock;

import com.alibaba.jvm.sandbox.repeater.plugin.domain.Identity;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeatContext;

/**
 * @author zhuangpeng
 * @since 2020/11/28
 */
public interface MockFilterHandler {

    /**
     * 是否需要mock
     */
    boolean needMock(Identity identity, RepeatContext context);
}
