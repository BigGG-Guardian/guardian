package com.sun.guardian.rate.limit.core.storage;

import com.sun.guardian.rate.limit.core.domain.token.RateLimitToken;

/**
 * 接口限流存储接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 16:15
 */
@FunctionalInterface
public interface RateLimitStorage {

    /**
     * 尝试获取限流许可
     *
     * @param token 限流令牌
     * @return {@code true} 允许通过，{@code false} 触发限流
     */
    boolean tryAcquire(RateLimitToken token);
}
