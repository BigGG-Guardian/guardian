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

    /**
     * 全局默认防重间隔（注解未显式指定时使用此值）
     */
    default int getInterval() { return 5; }

    /**
     * 全局默认时间单位
     */
    default TimeUnit getTimeUnit() { return TimeUnit.SECONDS; }

    /**
     * 全局默认拦截提示信息
     */
    default String getMessage() { return "您的请求过于频繁，请稍后再试"; }

    /**
     * 全局默认防重维度
     */
    default KeyScope getKeyScope() { return KeyScope.USER; }

    /**
     * 全局默认客户端类型
     */
    default ClientType getClientType() { return ClientType.PC; }
}
