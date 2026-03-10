package com.sun.guardian.core.exception;

/**
 * 请求解密异常
 *
 * @author scj
 * @version java version 1.8
 */
public class DecryptException extends RuntimeException {

    /**
     * 构造请求解密异常
     */
    public DecryptException(String message) {
        super(message);
    }
}
