package com.sun.guardian.api.switchs.core.config;

import com.sun.guardian.core.service.base.BaseConfig;

import java.util.List;

/**
 * 接口开关配置属性接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-28 21:33
 */
public interface ApiSwitchConfig extends BaseConfig {

    /**
     * 提示信息 Getter
     */
    String getMessage();

    /**
     * 默认关闭的接口 Getter
     */
    List<String> getDisabledUrls();

}
