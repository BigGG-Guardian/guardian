package com.sun.guardian.encrypt.core.service.decrypt;

import com.sun.guardian.core.utils.digest.DigestUtils;
import com.sun.guardian.encrypt.core.enums.encrypt.KeyEncryptAlgorithm;
import org.springframework.stereotype.Service;

/**
 * 密钥SM2解密接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 19:51
 */
@Service
public class KeySm2DecryptService implements KeyDecryptService {

    @Override
    public String decrypt(String encryptedData, String privateKey) throws Exception {
        return DigestUtils.sm2Decrypt(encryptedData, privateKey);
    }


    @Override
    public KeyEncryptAlgorithm getAlgorithm() {
        return KeyEncryptAlgorithm.SM2;
    }
}
