package com.sun.guardian.idempotent.core.service.token;

/**
 * 接口幂等Token生成接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 15:13
 */
@FunctionalInterface
public interface IdempotentTokenGenerator {

    /**
     * 生成Token
     *
     * @return token
     */
    String generate();

}
