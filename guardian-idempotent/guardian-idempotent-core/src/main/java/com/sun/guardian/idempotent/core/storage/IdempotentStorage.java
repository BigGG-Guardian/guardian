package com.sun.guardian.idempotent.core.storage;

import com.sun.guardian.idempotent.core.domain.token.IdempotentToken;

/**
 * 接口幂等存储接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 16:45
 */
public interface IdempotentStorage {

    /**
     * 保存Token
     *
     * @param token token信息
     */
    void save(IdempotentToken token);

    /**
     * 消费Token（原子操作，成功一次后删除）
     *
     * @param tokenKey 存储Key
     * @return true 首次消费成功，false 不存在或已消费
     */
    boolean tryConsume(String tokenKey);
}
