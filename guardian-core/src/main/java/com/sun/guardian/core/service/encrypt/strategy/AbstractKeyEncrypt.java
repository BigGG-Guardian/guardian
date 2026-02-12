package com.sun.guardian.core.service.encrypt.strategy;

/**
 * 抽象防重键加密基类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 20:39
 */
public abstract class AbstractKeyEncrypt {

    /**
     * 对防重键进行加密/摘要
     */
    public abstract String encrypt(String key);
}
