package com.sun.guardian.repeat.submit.core.config;

import com.sun.guardian.core.service.base.BaseConfig;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.repeat.submit.core.enums.client.ClientType;
import com.sun.guardian.repeat.submit.core.enums.scope.KeyScope;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 防重配置属性接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-22 10:09
 */
public interface RepeatSubmitConfig extends BaseConfig {

    /**
     * URL 防重规则 Getter
     */
    List<RepeatSubmitRule> getUrls();
}
