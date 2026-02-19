package com.sun.guardian.idempotent.core.storage;

import com.sun.guardian.idempotent.core.domain.token.IdempotentToken;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 接口幂等本地存储
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 16:52
 */
public class IdempotentLocalStorage implements IdempotentStorage {

    private final ConcurrentHashMap<String, Long> cache = new ConcurrentHashMap<>();

    {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "guardian-idempotent-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(this::cleanup, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void save(IdempotentToken token) {
        cache.put(token.getKey(), System.currentTimeMillis() + token.getTimeUnit().toMillis(token.getTimeout()));
    }

    @Override
    public boolean tryConsume(String tokenKey) {
        Long expireAt = cache.remove(tokenKey);
        if (expireAt == null) {
            return false;
        }
        return System.currentTimeMillis() <= expireAt;
    }

    /**
     * 清除过期token
     */
    private void cleanup() {
        long now = System.currentTimeMillis();
        cache.forEach((key, expireAt) -> {
            if (now > expireAt) {
                cache.remove(key, expireAt);
            }
        });
    }
}
