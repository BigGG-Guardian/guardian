package com.sun.guardian.rate.limit.core.config;

import com.sun.guardian.core.service.base.BaseConfig;
import com.sun.guardian.rate.limit.core.domain.rule.RateLimitRule;

import java.util.List;

/**
 * 接口限流配置属性接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-22 10:48
 */
public interface RateLimitConfig extends BaseConfig {

    /**
     * URL 限流规则 Getter
     */
    List<RateLimitRule> getUrls();
}
