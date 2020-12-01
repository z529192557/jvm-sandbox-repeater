package com.alibaba.jvm.sandbox.repeater.plugin.core.config;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.alibaba.jvm.sandbox.repeater.plugin.Constants;
import com.alibaba.jvm.sandbox.repeater.plugin.annotation.ConfigActive;
import com.alibaba.jvm.sandbox.repeater.plugin.api.Broadcaster;
import com.alibaba.jvm.sandbox.repeater.plugin.api.ConfigManager;
import com.alibaba.jvm.sandbox.repeater.plugin.api.EnvAware;
import com.alibaba.jvm.sandbox.repeater.plugin.api.Tracer;
import com.alibaba.jvm.sandbox.repeater.plugin.core.classloader.RepeaterLibClassLoader;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.AbstractBroadcaster;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultBroadcaster;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultConfigManager;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultEnvAware;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.standalone.StandaloneBroadcaster;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.DefaultTracer;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.JarFileUtil;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.PathUtils;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.PropertyUtil;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.SPILoader;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterConfig;
import com.alibaba.jvm.sandbox.repeater.plugin.spi.MockInterceptor;

import com.google.common.collect.Lists;
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

    public Broadcaster getBroadCaster(RepeaterConfig repeaterConfig) {
        Broadcaster selectedBroadcaster = null;
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(repeaterLibClassLoader);
            List<AbstractBroadcaster> broadcasters = SPILoader.loadSPI(AbstractBroadcaster.class,repeaterLibClassLoader);
            for(AbstractBroadcaster broadcaster : broadcasters){
                ConfigActive configActive =  broadcaster.getClass().getAnnotation(ConfigActive.class);
                if(null != configActive && null != repeaterConfig.getBroadcasterConfig() && configActive.value().equals(repeaterConfig.getBroadcasterConfig().getName())){
                    selectedBroadcaster = broadcaster;
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
        if(null == selectedBroadcaster){
            boolean standaloneMode = Boolean.valueOf(
                PropertyUtil.getPropertyOrDefault(Constants.REPEAT_STANDALONE_MODE, "false"));
            if(standaloneMode){
                selectedBroadcaster = new StandaloneBroadcaster();
            }else{
                selectedBroadcaster = new DefaultBroadcaster();
            }
        }
        selectedBroadcaster.start();;
        return selectedBroadcaster;
    }

    public List<MockInterceptor> getMockInterceptor() {
            List<MockInterceptor> interceptors = Lists.newArrayList();
            ServiceLoader<MockInterceptor> sl = ServiceLoader.load(MockInterceptor.class, repeaterLibClassLoader);
            final Iterator<MockInterceptor> iterator = sl.iterator();
            while (iterator.hasNext()) {
                interceptors.add(iterator.next());
            }
            return interceptors;
    }


    public EnvAware getEnvAware(RepeaterConfig repeaterConfig){
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(repeaterLibClassLoader);
            List<EnvAware> envAwares = SPILoader.loadSPI(EnvAware.class,repeaterLibClassLoader);
            for(EnvAware envAware : envAwares){
                ConfigActive configActive =  envAware.getClass().getAnnotation(ConfigActive.class);
                if(null != configActive && configActive.value().equals(repeaterConfig.getEnvAware())){
                    return envAware;
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
        return new DefaultEnvAware();
    }
}
