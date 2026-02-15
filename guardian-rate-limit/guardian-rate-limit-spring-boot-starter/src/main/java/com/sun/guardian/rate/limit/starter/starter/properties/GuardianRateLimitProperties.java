package com.sun.guardian.rate.limit.starter.starter.properties;

import com.sun.guardian.core.enums.key.KeyGeneratorType;
import com.sun.guardian.core.enums.response.ResponseMode;
import com.sun.guardian.core.enums.storage.StorageType;
import com.sun.guardian.rate.limit.core.domain.rule.RateLimitRule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 限流配置属性（{@code guardian.rate-limit}）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:40
 */
@Data
@ConfigurationProperties(prefix = "guardian.rate-limit")
public class GuardianRateLimitProperties {

    /** 总开关（默认 true） */
    private boolean enabled = true;

    /** 存储类型：redis / local */
    private StorageType storage = StorageType.REDIS;

    /** 键生成策略 */
    private KeyGeneratorType keyGenerator = KeyGeneratorType.DEFAULT;

    /** 响应模式：exception / json */
    private ResponseMode responseMode = ResponseMode.EXCEPTION;

    /** URL 限流规则（优先级高于 @RateLimit 注解） */
    private List<RateLimitRule> urls = new ArrayList<>();

    /** 排除规则（白名单），优先级最高 */
    private List<String> excludeUrls = new ArrayList<>();

    /** 是否打印拦截日志（默认 false） */
    private boolean logEnabled = false;
}
