package com.alibaba.jvm.sandbox.repeater.broadcaster.mq;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.alibaba.jvm.sandbox.repeater.plugin.core.model.ApplicationModel;

import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.manager.ManagerListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 *
 * @author yuebing.zyb@alibaba-inc.com 2017/5/24 20:39.
 */
public class EnvUtil {


    private static final Logger LOGGER = LoggerFactory.getLogger(EnvUtil.class);


    public static boolean isDaily() {
        return "daily".equals(ApplicationModel.instance().getEnvironment());
    }

    public static boolean isPrepare() {
        return "prepare".equals(ApplicationModel.instance().getEnvironment());
    }

    public static boolean isOnline() {
        return "online".equals(ApplicationModel.instance().getEnvironment());
    }

}
