package com.sun.guardian.core.exception;

/**
 * IP黑名单异常
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-23 14:43
 */
public class IpForbiddenException extends RuntimeException {

    /**
     * 构造IP黑名单异常
     */
    public IpForbiddenException(String message) {
        super(message);
    }
}
