package com.sun.guardian.core.exception;

/**
 * 接口开关异常
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-28 21:38
 */
public class ApiDisabledException extends RuntimeException {

    /**
     * 构造接口开关异常
     */
    public ApiDisabledException(String message) {
        super(message);
    }
}
