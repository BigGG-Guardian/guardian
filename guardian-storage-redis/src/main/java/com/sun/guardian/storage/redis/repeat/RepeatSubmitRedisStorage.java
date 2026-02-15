package com.sun.guardian.storage.redis.repeat;

import com.sun.guardian.repeat.submit.core.domain.token.RepeatSubmitToken;
import com.sun.guardian.repeat.submit.core.storage.RepeatSubmitStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 防重提交存储 - Redis（SET NX EX）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:13
 */
@RequiredArgsConstructor
public class RepeatSubmitRedisStorage implements RepeatSubmitStorage {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean tryAcquire(RepeatSubmitToken token) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(token.getKey(),
                        String.valueOf(token.getCreateTime()),
                        token.getTimeout(),
                        token.getTimeoutUnit());
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void release(RepeatSubmitToken token) {
        redisTemplate.delete(token.getKey());
    }
}
