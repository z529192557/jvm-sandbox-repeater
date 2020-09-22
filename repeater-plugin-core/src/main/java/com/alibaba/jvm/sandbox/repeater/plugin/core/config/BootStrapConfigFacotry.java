package com.alibaba.jvm.sandbox.repeater.plugin.core.config;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.alibaba.jvm.sandbox.repeater.plugin.annotation.ConfigActive;
import com.alibaba.jvm.sandbox.repeater.plugin.api.ConfigManager;
import com.alibaba.jvm.sandbox.repeater.plugin.core.classloader.RepeaterLibClassLoader;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultConfigManager;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.JarFileUtil;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.PathUtils;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.SPILoader;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuangpeng
 * @since 2020/9/21
 */
public class BootStrapConfigFacotry {

    private final static Logger log = LoggerFactory.getLogger(BootStrapConfigFacotry.class);

    private RepeaterLibClassLoader repeaterLibClassLoader;

    public BootStrapConfigFacotry(){
       String libPath = PathUtils.getLibPath();
       URL[] libs = JarFileUtil.getURLs(libPath,"repeater_lib");
       repeaterLibClassLoader = new RepeaterLibClassLoader(libs,BootStrapConfigFacotry.class.getClassLoader());
    }

    public ConfigManager getConfigManager(String bootstarpConfig) {

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(repeaterLibClassLoader);
            List<ConfigManager> configManagerList = SPILoader.loadSPI(ConfigManager.class,repeaterLibClassLoader);
            for(ConfigManager configManager : configManagerList){
                ConfigActive configActive =  configManager.getClass().getAnnotation(ConfigActive.class);
                if(null != configActive && configActive.value().equals(bootstarpConfig)){
                    return configManager;
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
        return new DefaultConfigManager();
    }
}
