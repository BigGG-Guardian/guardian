package com.sun.guardian.encrypt.core.service.encrypt;

import com.sun.guardian.encrypt.core.enums.encrypt.DataEncryptAlgorithm;

/**
 * 数据加密接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-09 11:53
 */
public interface DataEncryptService {
    /**
     * 加密数据
     *
     * @param data          数据
     * @param key           密钥
     * @return 明文
     */
    String encrypt(String data, String key) throws Exception;

    /**
     * 获取加密算法
     *
     * @return 加密算法
     */
    DataEncryptAlgorithm getAlgorithm();
}
