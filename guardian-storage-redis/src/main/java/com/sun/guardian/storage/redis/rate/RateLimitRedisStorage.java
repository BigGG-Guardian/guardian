package com.sun.guardian.storage.redis.rate;

import com.sun.guardian.rate.limit.core.domain.token.RateLimitToken;
import com.sun.guardian.rate.limit.core.enums.algorithm.RateLimitAlgorithm;
import com.sun.guardian.rate.limit.core.storage.RateLimitStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * 限流存储 - Redis
 * <p>
 * 滑动窗口：ZSET（score=时间戳，member=时间戳-随机数），令牌桶：HASH（tokens + lastRefill）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:13
 */
@RequiredArgsConstructor
public class RateLimitRedisStorage implements RateLimitStorage {

    private final StringRedisTemplate redisTemplate;

    // ==================== 滑动窗口 Lua ====================

    /**
     * KEYS[1]=限流Key, ARGV[1]=now, ARGV[2]=windowStart, ARGV[3]=maxCount, ARGV[4]=expireSeconds
     * <br>返回 1=放行 0=拒绝
     */
    private static final DefaultRedisScript<Long> SLIDING_WINDOW_SCRIPT;

    static {
        SLIDING_WINDOW_SCRIPT = new DefaultRedisScript<>();
        SLIDING_WINDOW_SCRIPT.setScriptText(
                "local key = KEYS[1]\n" +
                "local now = tonumber(ARGV[1])\n" +
                "local windowStart = tonumber(ARGV[2])\n" +
                "local maxCount = tonumber(ARGV[3])\n" +
                "local expireSeconds = tonumber(ARGV[4])\n" +
                "redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)\n" +
                "local current = redis.call('ZCARD', key)\n" +
                "if current < maxCount then\n" +
                "    redis.call('ZADD', key, now, tostring(now) .. '-' .. math.random(100000))\n" +
                "    redis.call('EXPIRE', key, expireSeconds)\n" +
                "    return 1\n" +
                "else\n" +
                "    return 0\n" +
                "end"
        );
        SLIDING_WINDOW_SCRIPT.setResultType(Long.class);
    }

    // ==================== 令牌桶 Lua ====================

    /**
     * KEYS[1]=限流Key, ARGV[1]=ratePerSecond, ARGV[2]=capacity, ARGV[3]=now(ms), ARGV[4]=expireSeconds
     * <br>返回 1=放行 0=拒绝
     */
    private static final DefaultRedisScript<Long> TOKEN_BUCKET_SCRIPT;

    static {
        TOKEN_BUCKET_SCRIPT = new DefaultRedisScript<>();
        TOKEN_BUCKET_SCRIPT.setScriptText(
                "local key = KEYS[1]\n" +
                "local ratePerSecond = tonumber(ARGV[1])\n" +
                "local capacity = tonumber(ARGV[2])\n" +
                "local now = tonumber(ARGV[3])\n" +
                "local expireSeconds = tonumber(ARGV[4])\n" +
                "local last = redis.call('HMGET', key, 'tokens', 'lastRefill')\n" +
                "local tokens = tonumber(last[1])\n" +
                "local lastRefill = tonumber(last[2])\n" +
                "if tokens == nil then\n" +
                "    tokens = capacity\n" +
                "    lastRefill = now\n" +
                "end\n" +
                "local elapsed = (now - lastRefill) / 1000\n" +
                "tokens = math.min(capacity, tokens + elapsed * ratePerSecond)\n" +
                "local allowed = 0\n" +
                "if tokens >= 1 then\n" +
                "    tokens = tokens - 1\n" +
                "    allowed = 1\n" +
                "end\n" +
                "redis.call('HMSET', key, 'tokens', tostring(tokens), 'lastRefill', tostring(now))\n" +
                "redis.call('EXPIRE', key, expireSeconds)\n" +
                "return allowed"
        );
        TOKEN_BUCKET_SCRIPT.setResultType(Long.class);
    }

    @Override
    public boolean tryAcquire(RateLimitToken token) {
        if (token.getAlgorithm() == RateLimitAlgorithm.TOKEN_BUCKET) {
            return tryAcquireTokenBucket(token);
        }
        return tryAcquireSlidingWindow(token);
    }

    private boolean tryAcquireSlidingWindow(RateLimitToken token) {
        long now = System.currentTimeMillis();
        long windowStart = now - token.getWindowMillis();

        Long result = redisTemplate.execute(SLIDING_WINDOW_SCRIPT,
                Collections.singletonList(token.getKey()),
                String.valueOf(now),
                String.valueOf(windowStart),
                String.valueOf(token.getMaxCount()),
                String.valueOf(token.getWindowSeconds() + 1));

        return result != null && result == 1L;
    }

    private boolean tryAcquireTokenBucket(RateLimitToken token) {
        long now = System.currentTimeMillis();

        Long result = redisTemplate.execute(TOKEN_BUCKET_SCRIPT,
                Collections.singletonList(token.getKey()),
                String.valueOf(token.getRefillRatePerSecond()),
                String.valueOf(token.getEffectiveCapacity()),
                String.valueOf(now),
                String.valueOf(token.getBucketExpireSeconds()));

        return result != null && result == 1L;
    }
}
