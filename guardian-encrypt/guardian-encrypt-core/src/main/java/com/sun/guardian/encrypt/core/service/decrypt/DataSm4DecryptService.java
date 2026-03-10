package com.sun.guardian.encrypt.core.service.decrypt;

import com.sun.guardian.core.utils.digest.DigestUtils;
import com.sun.guardian.encrypt.core.enums.encrypt.DataEncryptAlgorithm;
import org.springframework.stereotype.Service;

/**
 * 数据解密接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-06 15:55
 */
@Service
public class DataSm4DecryptService implements DataDecryptService {

    @Override
    public String decrypt(String encryptedData, String key) throws Exception {
        return DigestUtils.sm4Decrypt(encryptedData, key);
    }

    @Override
    public DataEncryptAlgorithm getAlgorithm() {
        return DataEncryptAlgorithm.SM4;
    }
}
