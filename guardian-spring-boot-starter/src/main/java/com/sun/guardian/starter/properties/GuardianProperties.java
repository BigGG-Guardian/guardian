package com.sun.guardian.starter.properties;

import com.sun.guardian.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.starter.enums.encrypt.KeyEncryptType;
import com.sun.guardian.starter.enums.key.KeyGeneratorType;
import com.sun.guardian.starter.enums.storage.StorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Guardian 配置属性
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:40
 */
@Data
@ConfigurationProperties(prefix = "guardian")
public class GuardianProperties {

    /**
     * 存储类型：redis / local（默认 redis）
     */
    private StorageType storage = StorageType.REDIS;

    /**
     * 全局默认防重键生成策略
     */
    private KeyGeneratorType keyGenerator = KeyGeneratorType.DEFAULT;

    /**
     * 全局默认防重键加密策略
     */
    private KeyEncryptType keyEncrypt = KeyEncryptType.NONE;

    /**
     * yml 批量配置的防重 URL 规则（优先级高于 @RepeatSubmit 注解）
     * <pre>
     * guardian:
     *   urls:
     *     - pattern: /api/order/submit
     *       interval: 10
     *       time-unit: seconds
     *       message: "请勿重复提交订单"
     *     - pattern: /api/payment/**
     *       interval: 30
     * </pre>
     */
    private List<RepeatSubmitRule> urls = new ArrayList<>();
}
