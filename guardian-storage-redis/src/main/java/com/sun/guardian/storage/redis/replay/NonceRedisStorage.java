package com.sun.guardian.storage.redis.replay;

import com.sun.guardian.anti.replay.core.constants.AntiReplayConstants;
import com.sun.guardian.anti.replay.core.storage.NonceStorage;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Nonce Redis 存储实现
 * <p>
 * 使用 Redis {@code SET NX EX} 命令实现原子性记录与去重，
 * Key 过期后由 Redis 自动清理，适用于分布式部署环境。
 *
 * @author scj
 * @since 2026-02-27
 */
public class NonceRedisStorage implements NonceStorage {

    private final StringRedisTemplate redisTemplate;

    public NonceRedisStorage(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryAcquire(String nonce, long nonceTtl, TimeUnit nonceTtlUnit) {
        String fullKey = String.format(AntiReplayConstants.KEY_PREFIX, nonce);
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(fullKey, String.valueOf(System.currentTimeMillis()), nonceTtl, nonceTtlUnit));
    }
}
