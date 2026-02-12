package com.sun.guardian.core.exception;

/**
 * 防重键加密器未找到异常
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 21:19
 */
public class KeyEncryptNotFoundException extends RuntimeException {

    public KeyEncryptNotFoundException(String message) {
        super(message);
    }
}
