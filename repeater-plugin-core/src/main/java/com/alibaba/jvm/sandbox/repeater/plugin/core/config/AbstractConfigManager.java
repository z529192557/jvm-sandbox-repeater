package com.alibaba.jvm.sandbox.repeater.plugin.core.config;

import com.alibaba.jvm.sandbox.repeater.plugin.api.ConfigListener;
import com.alibaba.jvm.sandbox.repeater.plugin.api.ConfigManager;

/**
 * @author zhuangpeng
 * @since 2020/9/22
 */
public abstract class AbstractConfigManager implements ConfigManager {

    @Override
    public void registerConfigListener(ConfigListener configListener) {
    }
}
