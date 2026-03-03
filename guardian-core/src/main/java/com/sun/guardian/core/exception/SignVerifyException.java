package com.sun.guardian.core.exception;

/**
 * 参数签名异常
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-02 21:18
 */
public class SignVerifyException extends RuntimeException {
    /**
     * 构造参数签名异常
     */
    public SignVerifyException(String message) {
        super(message);
    }
}
