package com.sun.guardian.rate.limit.starter.properties;

import com.sun.guardian.core.properties.BaseGuardianProperties;
import com.sun.guardian.rate.limit.core.config.RateLimitConfig;
import com.sun.guardian.rate.limit.core.domain.rule.RateLimitRule;
import com.sun.guardian.rate.limit.core.enums.algorithm.RateLimitAlgorithm;
import com.sun.guardian.rate.limit.core.enums.scope.RateLimitKeyScope;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 限流配置属性（guardian.rate-limit）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "guardian.rate-limit")
public class GuardianRateLimitProperties extends BaseGuardianProperties implements RateLimitConfig {

    /**
     * 总开关（默认 true）
     */
    private boolean enabled = true;

    /**
     * URL 限流规则（优先级高于 @RateLimit 注解）
     */
    private List<RateLimitRule> urls = new ArrayList<>();

    /**
     * 排除规则（白名单），优先级最高，命中直接放行
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * 全局默认 QPS，注解未显式指定时使用此值（默认 10）
     */
    private int qps = 10;

    /**
     * 全局默认时间窗口（默认 1）
     */
    private int window = 1;

    /**
     * 全局默认时间窗口单位（默认秒）
     */
    private TimeUnit windowUnit = TimeUnit.SECONDS;

    /**
     * 全局默认限流算法（默认滑动窗口）
     */
    private RateLimitAlgorithm algorithm = RateLimitAlgorithm.SLIDING_WINDOW;

    /**
     * 全局默认令牌桶容量（≤0 时取 qps 值，默认 -1）
     */
    private int capacity = -1;

    /**
     * 全局默认限流维度（默认 GLOBAL）
     */
    private RateLimitKeyScope rateLimitScope = RateLimitKeyScope.GLOBAL;

    /**
     * 全局默认拦截提示信息
     */
    private String message = "请求过于频繁，请稍后再试";

    public GuardianRateLimitProperties() {
        setInterceptorOrder(1000);
    }
}
