package com.sun.guardian.storage.redis.idempotent;

import com.sun.guardian.idempotent.core.domain.token.IdempotentToken;
import com.sun.guardian.idempotent.core.storage.IdempotentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 接口幂等存储 - Redis
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:13
 */
@RequiredArgsConstructor
public class IdempotentRedisStorage implements IdempotentStorage {

    private final StringRedisTemplate redisTemplate;

    /**
     * 保存幂等令牌
     */
    @Override
    public void save(IdempotentToken token) {
        redisTemplate.opsForValue().set(token.getKey(), String.valueOf(token.getCreateTime()), token.getTimeout(), token.getTimeUnit());
    }

    /**
     * 尝试消费幂等令牌
     */
    @Override
    public boolean tryConsume(String tokenKey) {
        return Boolean.TRUE.equals(redisTemplate.delete(tokenKey));
    }
}
