package com.alibaba.jvm.sandbox.repeater.config.diamond;

import java.io.IOException;
import java.util.concurrent.Executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.jvm.sandbox.repeater.plugin.Constants;
import com.alibaba.jvm.sandbox.repeater.plugin.annotation.ConfigActive;
import com.alibaba.jvm.sandbox.repeater.plugin.api.ConfigListener;
import com.alibaba.jvm.sandbox.repeater.plugin.api.ConfigManager;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.ApplicationModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterConfig;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterResult;


import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.manager.ManagerListener;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuangpeng
 * @since 2020/9/21
 */
@ConfigActive(value = Constants.DIAMOND)
@MetaInfServices(ConfigManager.class)
public class DiamondConfigManager implements ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiamondConfigManager.class);

    private final static Object lock = new Object();

    ConfigListener configListener = null;

    /**
     * DIAMOND配置文件
     */
    public static final String CONFIG_DATA_ID = "com.taobao.repeater.cloud.native.config";

    public static final String APP_NAME = ApplicationModel.instance().getAppName();
    @Override
    public RepeaterResult<RepeaterConfig> pullConfig() {
        LOGGER.info("wait register diamond Listener");
        return RepeaterResult.builder().success(true).message("operate success").build();
    }

    @Override
    public void registerConfigListener(ConfigListener configListener) {
        this.configListener = configListener;
        LOGGER.info("module launched, waiting for pulling config {}:{} ", CONFIG_DATA_ID, APP_NAME);
        Diamond.addListener(CONFIG_DATA_ID, APP_NAME, diamondListener);
    }

    private ManagerListener diamondListener = new ManagerListener() {
        @Override
        public Executor getExecutor() {
            return null;
        }

        @Override
        public void receiveConfigInfo(String configInfo) {
            LOGGER.info("receive config from diamond:{}", configInfo);
            synchronized (lock) {
                try {
                    RepeaterConfig rc = JSON.parseObject(configInfo, RepeaterConfig.class);
                    if(null != DiamondConfigManager.this.configListener){
                        RepeaterResult<RepeaterConfig> result = RepeaterResult.builder().success(true).data(rc).message("operate success").build();
                        DiamondConfigManager.this.configListener.receiveRepeaterConfig(result);
                    }
                } catch (Exception e) {
                    LOGGER.error("parse config from diamond error", e);
                }
            }
        }
    };

    public DiamondConfigManager() {
        LOGGER.info("instantiate diamond config manager");
    }

}
