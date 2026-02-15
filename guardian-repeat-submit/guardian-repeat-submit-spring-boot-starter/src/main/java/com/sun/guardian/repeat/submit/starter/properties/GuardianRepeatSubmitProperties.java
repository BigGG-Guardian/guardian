package com.sun.guardian.repeat.submit.starter.properties;

import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.core.enums.response.ResponseMode;
import com.sun.guardian.repeat.submit.starter.enums.encrypt.KeyEncryptType;
import com.sun.guardian.core.enums.key.KeyGeneratorType;
import com.sun.guardian.core.enums.storage.StorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 防重配置属性（{@code guardian.repeat-submit}）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 17:40
 */
@Data
@ConfigurationProperties(prefix = "guardian.repeat-submit")
public class GuardianRepeatSubmitProperties {

    /** 存储类型：redis / local */
    private StorageType storage = StorageType.REDIS;

    /** 键生成策略 */
    private KeyGeneratorType keyGenerator = KeyGeneratorType.DEFAULT;

    /** 键加密策略 */
    private KeyEncryptType keyEncrypt = KeyEncryptType.NONE;

    /** 响应模式：exception / json */
    private ResponseMode responseMode = ResponseMode.EXCEPTION;

    /** URL 防重规则（优先级高于 @RepeatSubmit 注解） */
    private List<RepeatSubmitRule> urls = new ArrayList<>();

    /** 排除规则（白名单），优先级最高 */
    private List<String> excludeUrls = new ArrayList<>();

    /** 是否打印拦截日志（默认 false） */
    private boolean logEnabled = false;
}
