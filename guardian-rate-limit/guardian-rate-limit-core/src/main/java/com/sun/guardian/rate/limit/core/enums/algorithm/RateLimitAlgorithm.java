package com.sun.guardian.rate.limit.core.enums.algorithm;

/**
 * 限流算法枚举
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 16:57
 */
public enum RateLimitAlgorithm {
    /** 滑动窗口 */
    SLIDING_WINDOW,
    /**
     * 令牌桶（惰性补充，请求到来时按时间差补令牌）
     */
    TOKEN_BUCKET;
}
