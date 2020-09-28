package com.alibaba.jvm.sandbox.repeater.plugin.dubbo;

import java.io.IOException;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.InvokeEvent;
import com.alibaba.jvm.sandbox.repeater.plugin.Constants;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.AbstractRepeater;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.ApplicationModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuangpeng
 * @since 2020/9/23
 */
public class DubboRunTimeUtil {

    protected static Logger log = LoggerFactory.getLogger(DubboRunTimeUtil.class);

    public static volatile Boolean IS_ALI_DUBBO = null;

    public static volatile Boolean IS_KAOLA_DUBBO = null;

    public static volatile String DUBBO_VERSION = null;

    public static boolean isAliDubbo(){
        return IS_ALI_DUBBO;
    }

    public static boolean iskaolaDubbo(){
        return IS_KAOLA_DUBBO;
    }

    public static String getDubboVersion(){
        return DUBBO_VERSION;
    }



    public static void initDubboEnv() throws IOException, ClassNotFoundException {
        log.info("dubbo env init start");
        boolean aliDubbo = false;
        boolean kaolaDubbo = false;
        String version = "2.7.3";
        Class clazz = null;
        try {
            clazz =  Class.forName(Constants.DUBBO_CLASS);
        } catch (ClassNotFoundException e) {
            log.warn("can not find apache dubbo class");
            clazz = Class.forName(Constants.ALI_DUBBO_CLASS);
        }
        log.info("find dubbo class : {}",clazz.getCanonicalName());
        CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        JarFile jarFile = new JarFile(codeSource.getLocation().getPath());
        Manifest manifest = jarFile.getManifest();
        if(null != manifest && null != manifest.getMainAttributes()){
            String title = manifest.getMainAttributes().getValue("Implementation-Title");
            if(null != title && title.contains(Constant.DUBBOK)){
                kaolaDubbo = true;
                log.info("current dubbo branch is dubbok");
            }
            String implementationVersion = manifest.getMainAttributes().getValue("Implementation-Version");
            if(null != implementationVersion){
                version = implementationVersion;
                log.info("current dubbo version is {}",version);
            }
            String vendorId = manifest.getMainAttributes().getValue("Implementation-Vendor-Id");
            if(null != vendorId){
                if(vendorId.startsWith(Constant.ALI_DUBBO_PACKAGE)){
                    aliDubbo = true;
                    log.info("current dubbo packaget is {}",vendorId);
                }
            }
        }

        IS_ALI_DUBBO = aliDubbo;
        IS_KAOLA_DUBBO = kaolaDubbo;
        DUBBO_VERSION = version;

    }
}
