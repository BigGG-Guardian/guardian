package com.sun.guardian.storage.redis.idempotent;

import com.sun.guardian.idempotent.core.domain.result.IdempotentResult;
import com.sun.guardian.idempotent.core.storage.IdempotentResultCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 接口幂等返回值Redis存储
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 20:07
 */
@RequiredArgsConstructor
public class IdempotentRedisResultCache implements IdempotentResultCache {
    private final StringRedisTemplate redisTemplate;

    @Override
    public void cacheResult(IdempotentResult result) {
        redisTemplate.opsForValue().set(result.getKey() + ":result", result.getJsonResult(), result.getTimeout(), result.getTimeUnit());
    }

    @Override
    public String getCacheResult(String key) {
        return redisTemplate.opsForValue().get(key + ":result");
    }
}
