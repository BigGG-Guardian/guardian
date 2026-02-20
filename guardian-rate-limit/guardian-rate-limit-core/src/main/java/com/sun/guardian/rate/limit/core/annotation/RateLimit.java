package com.sun.guardian.rate.limit.core.annotation;

import com.sun.guardian.rate.limit.core.enums.algorithm.RateLimitAlgorithm;
import com.sun.guardian.rate.limit.core.enums.scope.RateLimitKeyScope;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 16:05
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流数量
     *
     * 滑动窗口：QPS，窗口内最大请求数 = qps × window(秒)，令牌桶：每 window 补充的令牌数，补充速率 = qps / window(秒)
     */
    int qps() default 10;

    /**
     * 时间窗口
     *
     * 滑动窗口：窗口跨度，令牌桶：补充周期
     */
    int window() default 1;

    /**
     * 时间窗口单位（两种算法均使用）
     */
    TimeUnit windowUnit() default TimeUnit.SECONDS;

    /**
     * 被限流时的提示信息
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 限流维度
     */
    RateLimitKeyScope rateLimitScope() default RateLimitKeyScope.GLOBAL;

    /** 限流算法（默认滑动窗口） */
    RateLimitAlgorithm algorithm() default RateLimitAlgorithm.SLIDING_WINDOW;

    /**
     * 令牌桶容量（仅令牌桶算法），<= 0 时取 qps 值
     */
    int capacity() default -1;

}
