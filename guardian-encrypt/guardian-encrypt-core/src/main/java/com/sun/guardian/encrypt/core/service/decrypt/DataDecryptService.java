package com.sun.guardian.encrypt.core.service.decrypt;

import com.sun.guardian.encrypt.core.enums.encrypt.DataEncryptAlgorithm;

/**
 * 数据解密接口
 * @author scj
 * @version java version 1.8
 * @since 2026-03-06 15:55
 */
public interface DataDecryptService {
    /**
     * 解密数据
     *
     * @param encryptedData 密文数据
     * @param key           密钥
     * @return 明文
     */
    String decrypt(String encryptedData, String key) throws Exception;

    /**
     * 获取解密算法
     *
     * @return 解密算法
     */
    DataEncryptAlgorithm getAlgorithm();
}
