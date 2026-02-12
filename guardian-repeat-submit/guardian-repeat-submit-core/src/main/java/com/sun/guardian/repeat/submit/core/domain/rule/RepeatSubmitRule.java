package com.sun.guardian.repeat.submit.core.domain.rule;

import com.sun.guardian.repeat.submit.core.annotation.RepeatSubmit;
import com.sun.guardian.repeat.submit.core.enums.client.ClientType;
import com.sun.guardian.repeat.submit.core.enums.scope.KeyScope;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

/**
 * 防重提交规则（注解和 yml 配置的统一抽象）
 *
 * @author scj
 * @since 2026-02-09
 */
@Data
@Accessors(chain = true)
public class RepeatSubmitRule {

    /**
     * URL 匹配模式（仅 yml 配置时使用，支持 AntPath 通配符）
     */
    private String pattern;

    /**
     * 防重时间间隔
     */
    private int interval = 5;

    /**
     * 时间单位
     */
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * 错误提示信息
     */
    private String message = "您的请求过于频繁，请稍后再试";

    /**
     * 防重键维度
     */
    private KeyScope keyScope = KeyScope.USER;

    /**
     * 客户端类型
     */
    private ClientType clientType = ClientType.PC;

    /**
     * 从 @RepeatSubmit 注解创建规则
     */
    public static RepeatSubmitRule fromAnnotation(RepeatSubmit annotation) {
        return new RepeatSubmitRule()
                .setInterval(annotation.interval())
                .setTimeUnit(annotation.timeUnit())
                .setMessage(annotation.message())
                .setKeyScope(annotation.keyScope())
                .setClientType(annotation.clientType());
    }
}
