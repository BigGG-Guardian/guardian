package com.sun.guardian.core.exception;

/**
 * 请求加密异常
 *
 * @author scj
 * @version java version 1.8
 */
public class EncryptException extends RuntimeException {

    /**
     * 构造请求加密异常
     */
    public EncryptException(String message) {
        super(message);
    }
}
