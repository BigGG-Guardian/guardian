package com.sun.guardian.idempotent.core.storage;

import com.sun.guardian.idempotent.core.domain.result.IdempotentResult;

/**
 * 接口幂等返回值接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 19:54
 */
public interface IdempotentResultCache {

    /**
     * 缓存首次执行结果
     *
     * @param result 返回信息
     */
    void cacheResult(IdempotentResult result);

    /**
     * 获取缓存的执行结果
     *
     * @param key 幂等Key
     * @return 返回值Json字符串, 无缓存返回null
     */
    String getCacheResult(String key);
}
