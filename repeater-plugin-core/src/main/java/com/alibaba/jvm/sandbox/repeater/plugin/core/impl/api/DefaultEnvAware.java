package com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api;

import com.alibaba.jvm.sandbox.repeater.plugin.api.EnvAware;

import static com.alibaba.jvm.sandbox.repeater.plugin.core.util.PropertyUtil.getSystemPropertyOrDefault;

/**
 * @author zhuangpeng
 * @since 2020/10/21
 */
public class DefaultEnvAware implements EnvAware {

    @Override
    public String getEnv() {
        return getSystemPropertyOrDefault("project.env", "daily");
    }
}
