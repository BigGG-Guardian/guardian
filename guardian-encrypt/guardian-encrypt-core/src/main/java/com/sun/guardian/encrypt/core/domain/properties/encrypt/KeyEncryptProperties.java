package com.sun.guardian.encrypt.core.domain.properties.encrypt;

import lombok.Data;

/**
 * 密钥加密参数
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 18:57
 */
@Data
public class KeyEncryptProperties {

    /**
     * 公钥
     */
    private String publicKey = "";
}
