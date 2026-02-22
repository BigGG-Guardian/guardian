package com.sun.guardian.repeat.submit.starter.properties;

import com.sun.guardian.core.properties.BaseGuardianProperties;
import com.sun.guardian.repeat.submit.core.config.RepeatSubmitConfig;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.repeat.submit.starter.enums.encrypt.KeyEncryptType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 防重配置属性（guardian.repeat-submit）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "guardian.repeat-submit")
public class GuardianRepeatSubmitProperties extends BaseGuardianProperties implements RepeatSubmitConfig {

    /**
     * 总开关（默认 true）
     */
    private boolean enabled = true;

    /**
     * 键加密策略
     */
    private KeyEncryptType keyEncrypt = KeyEncryptType.NONE;

    /**
     * URL 防重规则（优先级高于 @RepeatSubmit 注解）
     */
    private List<RepeatSubmitRule> urls = new ArrayList<>();

    /**
     * 排除规则（白名单），优先级最高，命中直接放行
     */
    private List<String> excludeUrls = new ArrayList<>();

    public GuardianRepeatSubmitProperties() {
        setInterceptorOrder(2000);
    }
}
