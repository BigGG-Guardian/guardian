package com.sun.guardian.rate.limit.core.storage;

import com.sun.guardian.rate.limit.core.domain.token.RateLimitToken;
import com.sun.guardian.rate.limit.core.enums.algorithm.RateLimitAlgorithm;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 限流存储 - 本地缓存（单机）
 * <p>
 * 滑动窗口：{@link LinkedList} + synchronized 记录时间戳
 * <br>令牌桶：惰性补充，请求时按时间差计算令牌
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 16:27
 */
public class RateLimitLocalStorage implements RateLimitStorage {

    private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    /** 空闲超过 10 分钟的 key 会被清理 */
    private static final long IDLE_THRESHOLD_MS = 10 * 60 * 1000L;

    {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "guardian-rate-limit-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(this::cleanup, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public boolean tryAcquire(RateLimitToken token) {
        if (token.getAlgorithm() == RateLimitAlgorithm.TOKEN_BUCKET) {
            return tryAcquireTokenBucket(token);
        }
        return tryAcquireSlidingWindow(token);
    }

    // ==================== 滑动窗口 ====================

    private boolean tryAcquireSlidingWindow(RateLimitToken token) {
        long now = System.currentTimeMillis();
        long windowStart = now - token.getWindowMillis();
        long maxCount = token.getMaxCount();

        Deque<Long> deque = windows.computeIfAbsent(token.getKey(), k -> new LinkedList<>());

        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst() < windowStart) {
                deque.pollFirst();
            }

            if (deque.size() < maxCount) {
                deque.addLast(now);
                return true;
            }

            return false;
        }
    }

    // ==================== 令牌桶 ====================

    private boolean tryAcquireTokenBucket(RateLimitToken token) {
        int effectiveCapacity = token.getEffectiveCapacity();
        double ratePerSecond = token.getRefillRatePerSecond();

        TokenBucket bucket = buckets.computeIfAbsent(token.getKey(), k -> new TokenBucket(effectiveCapacity));

        synchronized (bucket) {
            long now = System.currentTimeMillis();
            double elapsedSeconds = (now - bucket.lastRefillTime) / 1000.0;
            bucket.tokens = Math.min(effectiveCapacity, bucket.tokens + elapsedSeconds * ratePerSecond);
            bucket.lastRefillTime = now;

            if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0;
                return true;
            }
            return false;
        }
    }

    private void cleanup() {
        long threshold = System.currentTimeMillis() - IDLE_THRESHOLD_MS;

        windows.forEach((key, deque) -> {
            synchronized (deque) {
                if (deque.isEmpty() || deque.peekLast() < threshold) {
                    windows.remove(key);
                }
            }
        });

        buckets.forEach((key, bucket) -> {
            synchronized (bucket) {
                if (bucket.lastRefillTime < threshold) {
                    buckets.remove(key);
                }
            }
        });
    }

    private static class TokenBucket {
        double tokens;
        long lastRefillTime;

        TokenBucket(int capacity) {
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }
    }
}
