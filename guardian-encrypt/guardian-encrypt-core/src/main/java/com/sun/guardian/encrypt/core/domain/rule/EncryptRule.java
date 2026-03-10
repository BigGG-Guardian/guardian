package com.sun.guardian.encrypt.core.domain.rule;

import com.sun.guardian.core.domain.BaseRule;
import com.sun.guardian.encrypt.core.enums.encrypt.DataEncryptAlgorithm;
import com.sun.guardian.encrypt.core.enums.encrypt.KeyEncryptAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 请求加密解密URL规则
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-06 10:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EncryptRule extends BaseRule {

    /**
     * 密钥解密算法
     */
    private KeyEncryptAlgorithm keyAlgorithm = KeyEncryptAlgorithm.RSA;

    /**
     * 数据解密算法
     */
    private DataEncryptAlgorithm dataAlgorithm = DataEncryptAlgorithm.AES;
}
