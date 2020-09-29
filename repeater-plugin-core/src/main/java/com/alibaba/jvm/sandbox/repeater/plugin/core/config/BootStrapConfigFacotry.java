package com.alibaba.jvm.sandbox.repeater.plugin.core.config;

import java.net.URL;
import java.util.List;

import com.alibaba.jvm.sandbox.repeater.plugin.annotation.ConfigActive;
import com.alibaba.jvm.sandbox.repeater.plugin.api.ConfigManager;
import com.alibaba.jvm.sandbox.repeater.plugin.api.Tracer;
import com.alibaba.jvm.sandbox.repeater.plugin.core.classloader.RepeaterLibClassLoader;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultConfigManager;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.DefaultTracer;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.JarFileUtil;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.PathUtils;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.SPILoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 引导配置工厂，根据配置负责加载各种实现
 *
 * @author zhuangpeng
 * @since 2020/9/21
 */
public class BootStrapConfigFacotry {

    private final static Logger log = LoggerFactory.getLogger(BootStrapConfigFacotry.class);

    private static BootStrapConfigFacotry instance = new BootStrapConfigFacotry();

    public static BootStrapConfigFacotry getInstance() {
        return instance;
    }

    private RepeaterLibClassLoader repeaterLibClassLoader;

    private BootStrapConfigFacotry(){
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

    public Tracer getTraceGenerator(String traceName) {
        if(null != traceName){
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(repeaterLibClassLoader);
                List<Tracer> tracers = SPILoader.loadSPI(Tracer.class,repeaterLibClassLoader);
                for(Tracer tracer : tracers){
                    ConfigActive configActive =  tracer.getClass().getAnnotation(ConfigActive.class);
                    if(null != configActive && configActive.value().equals(traceName)){
                        return tracer;
                    }
                }
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
        return new DefaultTracer();
    }
}
