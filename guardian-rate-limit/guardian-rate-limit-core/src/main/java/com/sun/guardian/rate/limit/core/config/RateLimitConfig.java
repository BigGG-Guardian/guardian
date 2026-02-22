package com.sun.guardian.rate.limit.core.config;

import com.sun.guardian.core.service.base.BaseConfig;
import com.sun.guardian.rate.limit.core.domain.rule.RateLimitRule;
import com.sun.guardian.rate.limit.core.enums.algorithm.RateLimitAlgorithm;
import com.sun.guardian.rate.limit.core.enums.scope.RateLimitKeyScope;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    /**
     * 全局默认 QPS（注解未显式指定时使用此值）
     */
    default int getQps() { return 10; }

    /**
     * 全局默认时间窗口
     */
    default int getWindow() { return 1; }

    /**
     * 全局默认时间窗口单位
     */
    default TimeUnit getWindowUnit() { return TimeUnit.SECONDS; }

    /**
     * 全局默认限流算法
     */
    default RateLimitAlgorithm getAlgorithm() { return RateLimitAlgorithm.SLIDING_WINDOW; }

    /**
     * 全局默认令牌桶容量（≤0 时取 qps 值）
     */
    default int getCapacity() { return -1; }

    /**
     * 全局默认限流维度
     */
    default RateLimitKeyScope getRateLimitScope() { return RateLimitKeyScope.GLOBAL; }

    /**
     * 全局默认拦截提示信息
     */
    default String getMessage() { return "请求过于频繁，请稍后再试"; }
}
