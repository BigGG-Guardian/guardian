package com.sun.guardian.encrypt.core.service.encrypt;

import com.sun.guardian.core.utils.digest.DigestUtils;
import com.sun.guardian.encrypt.core.enums.encrypt.KeyEncryptAlgorithm;
import org.springframework.stereotype.Service;

/**
 * 密钥SM2加密接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 19:51
 */
@Service
public class KeySm2EncryptService implements KeyEncryptService {

    @Override
    public String encrypt(String data, String privateKey) throws Exception {
        return DigestUtils.sm2Encrypt(data, privateKey);
    }


    @Override
    public KeyEncryptAlgorithm getAlgorithm() {
        return KeyEncryptAlgorithm.SM2;
    }
}
