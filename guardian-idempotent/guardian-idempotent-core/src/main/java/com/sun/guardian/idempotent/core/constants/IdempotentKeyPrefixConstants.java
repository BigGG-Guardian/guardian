package com.sun.guardian.idempotent.core.constants;

/**
 * 接口幂等常量
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 10:58
 */
public interface IdempotentKeyPrefixConstants {
    /**
     * 接口幂等存储前缀（前缀 + 接口标识 + Token）
     */
    String KEY_PREFIX = "guardian:idempotent:%s:%s";
}
