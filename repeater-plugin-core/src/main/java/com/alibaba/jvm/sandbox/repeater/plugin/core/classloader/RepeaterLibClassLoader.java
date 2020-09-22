package com.alibaba.jvm.sandbox.repeater.plugin.core.classloader;

import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * repeater lib classLader
 *
 * @author zhuangpeng
 * @since 2020/9/21
 */
public class RepeaterLibClassLoader extends URLClassLoader {

    private final static Logger log = LoggerFactory.getLogger(RepeaterLibClassLoader.class);
    
    public RepeaterLibClassLoader(URL[] urls,ClassLoader parent) {
        super(urls,parent);
    }
}


