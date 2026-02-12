package com.sun.guardian.repeat.submit.starter.properties;

import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitExcludeRule;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.repeat.submit.starter.enums.key.KeyGeneratorType;
import com.sun.guardian.repeat.submit.starter.enums.storage.StorageType;
import com.sun.guardian.repeat.submit.starter.enums.encrypt.KeyEncryptType;
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
     * 批量配置的防重 URL 规则（优先级高于 {@code @RepeatSubmit} 注解）
     * <pre>
     * guardian:
     *   urls:
     *     - pattern: /api/order/submit
     *       interval: 10
     *       time-unit: seconds
     *       message: "请勿重复提交订单"
     *       key-scope: user
     *     - pattern: /api/payment/**
     *       interval: 30
     * </pre>
     */
    private List<RepeatSubmitRule> urls = new ArrayList<>();

    /**
     * 排除规则（白名单），命中的 URL 跳过所有防重检查，优先级最高
     * <pre>
     * guardian:
     *   exclude-urls:
     *     - pattern: /api/public/**
     *     - pattern: /api/health
     * </pre>
     */
    private List<RepeatSubmitExcludeRule> excludeUrls = new ArrayList<>();
}
