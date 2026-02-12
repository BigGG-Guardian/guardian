package com.sun.guardian.redis.storage;

import com.sun.guardian.core.domain.token.RepeatSubmitToken;
import com.sun.guardian.core.storage.RepeatSubmitStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 防重提交存储-Redis
 * 基于 SET NX EX 实现原子性防重校验
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:13
 */
@RequiredArgsConstructor
public class RepeatSubmitRedisStorage implements RepeatSubmitStorage {

    private final StringRedisTemplate redisTemplate;

    /**
     * 尝试提交令牌
     *
     * @param token 防重令牌
     * @return true-允许提交 false-拒绝提交
     */
    @Override
    public boolean tryAcquire(RepeatSubmitToken token) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(token.getKey(),
                        String.valueOf(token.getCreateTime()),
                        token.getTimeout(),
                        token.getTimeoutUnit());
        return Boolean.TRUE.equals(success);
    }

    /**
     * 手动释放令牌
     *
     * @param token 防重令牌
     */
    @Override
    public void release(RepeatSubmitToken token) {
        redisTemplate.delete(token.getKey());
    }
}
