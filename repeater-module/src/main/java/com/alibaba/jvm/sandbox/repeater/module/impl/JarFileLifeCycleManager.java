package com.alibaba.jvm.sandbox.repeater.module.impl;

import java.io.File;
import java.net.URL;
import java.util.List;

import com.alibaba.jvm.sandbox.repeater.module.classloader.PluginClassLoader;
import com.alibaba.jvm.sandbox.repeater.plugin.api.LifecycleManager;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.JarFileUtil;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.SPILoader;
import com.alibaba.jvm.sandbox.repeater.plugin.spi.InvokePlugin;
import com.alibaba.jvm.sandbox.repeater.plugin.spi.Repeater;
import com.alibaba.jvm.sandbox.repeater.plugin.spi.SubscribeSupporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 *
 * @author zhaoyb1990
 */
public class JarFileLifeCycleManager implements LifecycleManager {

    private final static Logger log = LoggerFactory.getLogger(JarFileLifeCycleManager.class);

    private final PluginClassLoader classLoader;

    public JarFileLifeCycleManager(String jarFilePath, PluginClassLoader.Routing ... routingArray) {
        File file = new File(jarFilePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("jar file does not exist, path=" + jarFilePath);
        }
        final URL[] urLs = JarFileUtil.getURLs(jarFilePath,"repeater_plugin");
        if (urLs.length == 0) {
            throw new IllegalArgumentException("does not have any available jar in path:" + jarFilePath);
        }
        this.classLoader = new PluginClassLoader(urLs, this.getClass().getClassLoader(), routingArray);
    }

    @Override
    public List<InvokePlugin> loadInvokePlugins() {
        return SPILoader.loadSPI(InvokePlugin.class, classLoader);
    }

    @Override
    public List<Repeater> loadRepeaters() {
        return SPILoader.loadSPI(Repeater.class, classLoader);
    }

    @Override
    public List<SubscribeSupporter> loadSubscribes() {
        return SPILoader.loadSPI(SubscribeSupporter.class, classLoader);
    }

    @Override
    public void release() {
        classLoader.closeIfPossible();
    }
}
