package com.alibaba.jvm.sandbox.repeater.config.diamond;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.alibaba.jvm.sandbox.repeater.plugin.Constants;
import com.alibaba.jvm.sandbox.repeater.plugin.annotation.ConfigActive;
import com.alibaba.jvm.sandbox.repeater.plugin.api.ConfigManager;
import com.alibaba.jvm.sandbox.repeater.plugin.api.EnvAware;

import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.manager.ManagerListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuangpeng
 * @since 2020/10/21
 */
@ConfigActive(value = Constants.DIAMOND)
@MetaInfServices(EnvAware.class)
public class DiamondEnvAware implements EnvAware {

    public final static String CONFIG_GROUP = "place";
    public final static String CONFIG_ENV = "env";

    private static final Logger LOGGER = LoggerFactory.getLogger(DiamondEnvAware.class);

    private static String env = null;

    {
        Diamond.addListener(CONFIG_ENV, CONFIG_GROUP, new ManagerListenerAdapter() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                if (StringUtils.isBlank(configInfo)) {
                    env = configInfo;
                }
            }
        });
    }

    @Override
    public String getEnv() {
        if (StringUtils.isBlank(env)) {
            try {
                env = Diamond.getConfig(CONFIG_ENV, CONFIG_GROUP, 3000L);
            } catch (IOException e) {

            }
        }
        return env;
    }
}
