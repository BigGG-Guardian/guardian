package com.sun.guardian.idempotent.core.service.token;

/**
 * 接口幂等-Token接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 11:48
 */
@FunctionalInterface
public interface IdempotentTokenService {

    /**
     * 创建接口幂等Token
     *
     * @param key 接口唯一标识（对应 {@link com.sun.guardian.idempotent.core.annotation.Idempotent#value()}）
     * @return token
     */
    String createToken(String key);
}
