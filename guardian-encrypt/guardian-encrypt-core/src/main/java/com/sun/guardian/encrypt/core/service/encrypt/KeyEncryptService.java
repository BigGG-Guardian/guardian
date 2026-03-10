package com.sun.guardian.encrypt.core.service.encrypt;

import com.sun.guardian.encrypt.core.enums.encrypt.KeyEncryptAlgorithm;

/**
 * 密钥加密接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-09 11:53
 */
public interface KeyEncryptService {
    /**
     * 加密数据
     *
     * @param data          数据
     * @param privateKey    私钥
     * @return 明文
     */
    String encrypt(String data, String privateKey) throws Exception;

    /**
     * 获取加密算法
     *
     * @return 加密算法
     */
    KeyEncryptAlgorithm getAlgorithm();
}
