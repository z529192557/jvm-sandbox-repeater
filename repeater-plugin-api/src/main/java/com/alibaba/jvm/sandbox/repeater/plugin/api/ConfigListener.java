package com.alibaba.jvm.sandbox.repeater.plugin.api;

import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterConfig;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterResult;

/**
 * @author zhuangpeng
 * @since 2020/9/21
 */
public interface ConfigListener {

    /**
     * 收到来自配置中心推送的配置
     */
    void receiveRepeaterConfig(RepeaterResult<RepeaterConfig> repeaterResult);
}
