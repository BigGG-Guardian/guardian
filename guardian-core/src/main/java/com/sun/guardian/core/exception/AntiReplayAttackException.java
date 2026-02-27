package com.sun.guardian.core.exception;

/**
 * 防重放攻击异常
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-27 14:01
 */
public class AntiReplayAttackException extends RuntimeException {
    /**
     * 构造防重放攻击异常
     */
    public AntiReplayAttackException(String message) {
        super(message);
    }
}
