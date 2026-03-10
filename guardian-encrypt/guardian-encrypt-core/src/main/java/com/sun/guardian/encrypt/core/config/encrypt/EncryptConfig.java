package com.sun.guardian.encrypt.core.config.encrypt;

import com.sun.guardian.core.service.base.BaseConfig;
import com.sun.guardian.encrypt.core.domain.properties.DataProperties;
import com.sun.guardian.encrypt.core.domain.properties.encrypt.KeyEncryptProperties;
import com.sun.guardian.encrypt.core.domain.rule.EncryptRule;

import java.util.List;

/**
 * 请求加密配置属性接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 19:21
 */
public interface EncryptConfig extends BaseConfig {

    /**
     * 密钥加密参数 Getter
     */
    KeyEncryptProperties getKey();

    /**
     * 数据加密参数 Getter
     */
    DataProperties getData();

    /**
     * 需要加密的 URL 规则列表 Getter
     */
    List<EncryptRule> getUrls();

    /**
     * 返回值加密 Advice 排序 Getter
     */
    int getResultAdviceOrder();
}
