package com.sun.guardian.repeat.submit.core.storage;

import com.sun.guardian.repeat.submit.core.domain.token.RepeatSubmitToken;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 防重提交存储-本地缓存
 * 基于 ConcurrentHashMap 实现，适用于单机部署场景
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:30
 */
public class RepeatSubmitLocalStorage implements RepeatSubmitStorage {

    /**
     * key -> 过期时间戳（毫秒）
     */
    private final ConcurrentHashMap<String, Long> cache = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(RepeatSubmitToken token) {
        long expireAt = System.currentTimeMillis() + token.getTimeoutUnit().toMillis(token.getTimeout());
        Long existing = cache.putIfAbsent(token.getKey(), expireAt);
        if (existing == null) {
            return true;
        }
        if (System.currentTimeMillis() > existing) {
            cache.put(token.getKey(), expireAt);
            return true;
        }
        return false;
    }

    @Override
    public void release(RepeatSubmitToken token) {
        cache.remove(token.getKey());
    }
}
