package com.sun.guardian.rate.limit.starter.properties;

import com.sun.guardian.core.properties.BaseGuardianProperties;
import com.sun.guardian.rate.limit.core.domain.rule.RateLimitRule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 限流配置属性（guardian.rate-limit）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "guardian.rate-limit")
public class GuardianRateLimitProperties extends BaseGuardianProperties {

    /**
     * 总开关（默认 true）
     */
    private boolean enabled = true;

    /**
     * URL 限流规则（优先级高于 @RateLimit 注解）
     */
    private List<RateLimitRule> urls = new ArrayList<>();

    /**
     * 排除规则（白名单），优先级最高
     */
    private List<String> excludeUrls = new ArrayList<>();

    public GuardianRateLimitProperties() {
        setInterceptorOrder(1000);
    }
}
