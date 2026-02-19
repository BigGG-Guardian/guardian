package com.sun.guardian.rate.limit.core.domain.token;

import com.sun.guardian.rate.limit.core.enums.algorithm.RateLimitAlgorithm;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

/**
 * 限流令牌，承载一次限流判定的全部参数
 * <p>
 * 滑动窗口：{@code maxCount = qps × windowSeconds}
 * <br>令牌桶：{@code refillRate = qps / windowSeconds} 令牌/秒
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 16:17
 */
@Accessors(chain = true)
@Data
public class RateLimitToken {

    /**
     * 限流 Key
     */
    private String key;

    /**
     * 限流数量
     * <br>滑动窗口 = QPS，令牌桶 = 每 window 补充的令牌数
     */
    private int qps;

    /**
     * 时间窗口
     */
    private long window;

    /**
     * 时间窗口单位
     */
    private TimeUnit windowUnit;

    /**
     * 限流算法
     */
    private RateLimitAlgorithm algorithm = RateLimitAlgorithm.SLIDING_WINDOW;

    /**
     * 令牌桶容量，{@code <= 0} 时取 {@link #qps}
     */
    private int capacity;

    // ==================== 公共计算 ====================

    /**
     * 窗口时长（毫秒）
     */
    public long getWindowMillis() {
        return windowUnit.toMillis(window);
    }

    /**
     * 窗口时长（秒，最小 1）
     */
    public long getWindowSeconds() {
        return Math.max(1, windowUnit.toSeconds(window));
    }

    // ==================== 滑动窗口 ====================

    /**
     * 窗口内最大请求数：{@code qps × windowSeconds}
     */
    public long getMaxCount() {
        return (long) qps * getWindowSeconds();
    }

    // ==================== 令牌桶 ====================

    /**
     * 有效桶容量
     */
    public int getEffectiveCapacity() {
        return capacity > 0 ? capacity : qps;
    }

    /**
     * 每秒令牌补充速率：{@code qps / windowSeconds}
     * <p>
     * 如 qps=5, window=1min → 5/60 ≈ 0.083/秒
     */
    public double getRefillRatePerSecond() {
        return qps / (double) getWindowSeconds();
    }

    /**
     * 桶 Key 过期时间（秒）= 桶从空到满的时间 + 1s
     */
    public long getBucketExpireSeconds() {
        double rate = getRefillRatePerSecond();
        return (long) Math.ceil(getEffectiveCapacity() / rate) + 1;
    }
}
