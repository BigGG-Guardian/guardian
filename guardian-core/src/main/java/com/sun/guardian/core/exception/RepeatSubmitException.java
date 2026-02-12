package com.sun.guardian.core.exception;

/**
 * 重复提交异常
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 22:30
 */
public class RepeatSubmitException extends RuntimeException {

    public RepeatSubmitException() {
    }

    public RepeatSubmitException(String message) {
        super(message);
    }
}
