package com.sun.guardian.auto.trim.core.config;

import com.sun.guardian.core.service.base.BaseConfig;

import java.util.Set;

/**
 * 请求参数自动trim配置属性接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-22 11:20
 */
public interface AutoTrimConfig extends BaseConfig {

    /**
     * 排除字段列表（表单参数 + JSON body 字段统一生效） Getter
     */
    Set<String> getExcludeFields();
}
