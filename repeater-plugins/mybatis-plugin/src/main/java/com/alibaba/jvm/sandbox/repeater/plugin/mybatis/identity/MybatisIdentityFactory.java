package com.alibaba.jvm.sandbox.repeater.plugin.mybatis.identity;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Identity;

/**
 * @author zhuangpeng
 * @since 2020/11/5
 */
public interface MybatisIdentityFactory {

    /**
     * 获取Mybatis的Identity
     */
    Identity getIdentity(BeforeEvent event);
}
