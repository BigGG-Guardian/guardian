package com.sun.guardian.encrypt.core.domain.properties.decrypt;

import lombok.Data;

/**
 * 密钥解密参数
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 18:57
 */
@Data
public class KeyDecryptProperties {

    /**
     * 私钥
     */
    private String privateKey = "";
}
