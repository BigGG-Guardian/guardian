package com.sun.guardian.slow.api.core.config;

import com.sun.guardian.core.service.base.BaseConfig;

/**
 * 慢接口检测配置属性接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-22 11:08
 */
public interface SlowApiConfig extends BaseConfig {

    /**
     * 慢接口阈值 Getter
     */
    long getThreshold();
}
