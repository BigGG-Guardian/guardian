package com.sun.guardian.idempotent.core.config;

import com.sun.guardian.core.service.base.BaseConfig;

import java.util.concurrent.TimeUnit;

/**
 * 接口幂等配置属性接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-22 10:59
 */
public interface IdempotentConfig extends BaseConfig {

    /**
     * Token 有效期 Getter
     */
    long getTimeout();

    /**
     * Token 有效期单位 Getter
     */
    TimeUnit getTimeUnit();
}
