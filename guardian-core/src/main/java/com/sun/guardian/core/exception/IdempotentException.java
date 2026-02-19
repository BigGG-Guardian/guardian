package com.sun.guardian.core.exception;

/**
 * 接口幂等异常
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 11:04
 */
public class IdempotentException extends RuntimeException {

    public IdempotentException(String message) {
        super(message);
    }
}
