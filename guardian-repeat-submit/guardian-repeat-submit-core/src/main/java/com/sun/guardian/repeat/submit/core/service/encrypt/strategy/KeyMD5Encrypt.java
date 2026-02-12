package com.sun.guardian.repeat.submit.core.service.encrypt.strategy;

import cn.hutool.crypto.digest.MD5;

/**
 * MD5摘要策略
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 20:50
 */
public class KeyMD5Encrypt extends AbstractKeyEncrypt {

    @Override
    public String encrypt(String key) {
        return MD5.create().digestHex(key);
    }
}
