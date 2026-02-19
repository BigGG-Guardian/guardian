package com.sun.guardian.idempotent.core.storage;

import com.sun.guardian.idempotent.core.domain.result.IdempotentResult;
import com.sun.guardian.idempotent.core.domain.result.IdempotentResultEntity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 接口幂等返回值本地存储
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 19:58
 */
public class IdempotentLocalResultCache implements IdempotentResultCache {
    private final ConcurrentHashMap<String, IdempotentResultEntity> cache = new ConcurrentHashMap<>();

    {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "guardian-idempotent-result-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(this::cleanup, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void cacheResult(IdempotentResult result) {
        String resultKey = result.getKey() + ":result";
        long expireAt = System.currentTimeMillis() + result.getTimeUnit().toMillis(result.getTimeout());
        cache.put(resultKey, new IdempotentResultEntity()
                .setJsonResult(result.getJsonResult())
                .setExpireAt(expireAt));
    }

    @Override
    public String getCacheResult(String key) {
        String resultKey = key + ":result";
        IdempotentResultEntity resultEntity = cache.get(resultKey);
        if (resultEntity == null) {
            return null;
        }
        if (System.currentTimeMillis() > resultEntity.getExpireAt()) {
            cache.remove(resultKey, resultEntity);
            return null;
        }
        return resultEntity.getJsonResult();
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        cache.forEach((key, entry) -> {
            if (now > entry.getExpireAt()) {
                cache.remove(key, entry);
            }
        });
    }
}
