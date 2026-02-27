package com.sun.guardian.anti.replay.core.storage;

import java.util.concurrent.TimeUnit;

/**
 * Nonce 存储接口
 * <p>
 * 负责 Nonce 的原子性记录与去重判断，支持 Redis 和本地两种实现。
 * Nonce 一旦被记录，在 TTL 过期前同一 Nonce 的后续请求均视为重放。
 *
 * @author scj
 * @since 2026-02-27
 * @see com.sun.guardian.storage.redis.replay.NonceRedisStorage
 * @see NonceLocalStorage
 */
public interface NonceStorage {

    /**
     * 尝试记录 Nonce（原子操作）
     * <p>
     * 如果 Nonce 不存在则记录并返回 {@code true}（放行），
     * 如果 Nonce 已存在则返回 {@code false}（重放攻击）。
     *
     * @param nonce        请求唯一标识（UUID）
     * @param nonceTtl     nonce 存活时间，应远大于 timestamp 有效窗口
     * @param nonceTtlUnit nonce 存活时间单位
     * @return {@code true} 首次记录成功（放行），{@code false} 已存在（重放）
     */
    boolean tryAcquire(String nonce, long nonceTtl, TimeUnit nonceTtlUnit);
}
