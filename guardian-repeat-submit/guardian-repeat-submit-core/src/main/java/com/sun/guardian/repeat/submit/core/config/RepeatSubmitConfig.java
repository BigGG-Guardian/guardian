package com.sun.guardian.repeat.submit.core.config;

import com.sun.guardian.core.service.base.BaseConfig;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;

import java.util.List;

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
