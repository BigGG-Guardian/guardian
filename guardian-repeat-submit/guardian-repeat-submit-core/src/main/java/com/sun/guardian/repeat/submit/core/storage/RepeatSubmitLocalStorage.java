package com.sun.guardian.repeat.submit.core.storage;

import com.sun.guardian.repeat.submit.core.domain.token.RepeatSubmitToken;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 防重提交存储 - 本地缓存（ConcurrentHashMap）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:30
 */
public class RepeatSubmitLocalStorage implements RepeatSubmitStorage {

    /** key -> 过期时间戳（毫秒） */
    private final ConcurrentHashMap<String, Long> cache = new ConcurrentHashMap<>();

    {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "guardian-repeat-submit-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(this::cleanup, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public boolean tryAcquire(RepeatSubmitToken token) {
        long expireAt = System.currentTimeMillis() + token.getTimeoutUnit().toMillis(token.getTimeout());
        Long existing = cache.putIfAbsent(token.getKey(), expireAt);
        if (existing == null) {
            return true;
        }
        if (System.currentTimeMillis() > existing) {
            if (cache.replace(token.getKey(), existing, expireAt)) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void release(RepeatSubmitToken token) {
        cache.remove(token.getKey());
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        cache.forEach((key, expireAt) -> {
            if (now > expireAt) {
                cache.remove(key, expireAt);
            }
        });
    }
}
