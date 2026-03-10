package com.sun.guardian.encrypt.core.domain.properties;

import com.sun.guardian.encrypt.core.enums.mode.DataKeyMode;
import lombok.Data;

/**
 * 数据加密解密参数
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 18:57
 */
@Data
public class DataProperties {

    /**
     * param数据加密后别名
     */
    private String paramAlias = "encryptParam";

    /**
     * body数据加密后别名
     */
    private String bodyAlias = "encryptBody";

    /**
     * 数据密钥请求头/相应头名称
     */
    private String dataKeyHeader = "X-Encrypt-Key";

    /**
     * 密钥模式
     * STATIC: 静态密钥，需配置key参数
     * DYNAMIC: 每次请求动态生成密钥，无需配置key参数
     */
    private DataKeyMode keyMode = DataKeyMode.DYNAMIC;

    /**
     * 密钥
     */
    private String key = "";
}
