package com.sun.guardian.encrypt.core.config.decrypt;

import com.sun.guardian.core.service.base.BaseConfig;
import com.sun.guardian.encrypt.core.domain.properties.DataProperties;
import com.sun.guardian.encrypt.core.domain.properties.decrypt.KeyDecryptProperties;
import com.sun.guardian.encrypt.core.domain.rule.EncryptRule;

import java.util.List;

/**
 * 请求解密配置属性接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 19:21
 */
public interface DecryptConfig extends BaseConfig {

    /**
     * 密钥解密参数 Getter
     */
    KeyDecryptProperties getKey();

    /**
     * 数据解密参数 Getter
     */
    DataProperties getData();

    /**
     * 缺少数据密钥请求头 提示信息 Getter
     */
    String getMissingDataKeyHeaderMessage();

    /**
     * 需要解密的 URL 规则列表 Getter
     */
    List<EncryptRule> getUrls();
}
