package com.sun.guardian.encrypt.core.service.decrypt;

import com.sun.guardian.encrypt.core.enums.encrypt.KeyEncryptAlgorithm;

/**
 * 密钥解密接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 19:44
 */
public interface KeyDecryptService {

    /**
     * 解密数据
     *
     * @param encryptedData 密文数据
     * @param privateKey    私钥
     * @return 明文
     */
    String decrypt(String encryptedData, String privateKey) throws Exception;

    /**
     * 获取解密算法
     *
     * @return 解密算法
     */
    KeyEncryptAlgorithm getAlgorithm();

}
