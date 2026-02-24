package com.sun.guardian.core.exception;

/**
 * 接口限流异常
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 22:30
 */
public class RateLimitException extends RuntimeException {

    /**
     * 构造限流异常
     */
    public RateLimitException(String message) {
        super(message);
    }
}
