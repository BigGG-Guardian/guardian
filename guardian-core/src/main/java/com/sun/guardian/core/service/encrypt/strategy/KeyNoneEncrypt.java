package com.sun.guardian.core.service.encrypt.strategy;

/**
 * 不加密策略（原文透传）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 20:50
 */
public class KeyNoneEncrypt extends AbstractKeyEncrypt {

    @Override
    public String encrypt(String key) {
        return key;
    }
}
