package com.sun.guardian.anti.replay.core.storage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Nonce 本地存储实现
 * <p>
 * 使用 {@link ConcurrentHashMap} 存储 Nonce，通过后台守护线程定期清理过期条目。
 * 适用于单机部署或开发测试场景，分布式环境建议使用 Redis 实现。
 *
 * @author scj
 * @since 2026-02-27
 */
public class NonceLocalStorage implements NonceStorage {

    private final ConcurrentHashMap<String, Long> cache = new ConcurrentHashMap<>();

    {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "guardian-anti-replay-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(this::evict, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public boolean tryAcquire(String nonce, long nonceTtl, TimeUnit nonceTtlUnit) {
        long expireAt = System.currentTimeMillis() + nonceTtlUnit.toMillis(nonceTtl);
        Long existing = cache.putIfAbsent(nonce, expireAt);
        return existing == null;
    }

    private void evict() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(e -> now > e.getValue());
    }
}
