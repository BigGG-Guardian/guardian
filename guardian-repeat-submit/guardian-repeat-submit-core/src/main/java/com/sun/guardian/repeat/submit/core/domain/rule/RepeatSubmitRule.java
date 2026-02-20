package com.sun.guardian.repeat.submit.core.domain.rule;

import com.sun.guardian.core.domain.BaseRule;
import com.sun.guardian.repeat.submit.core.annotation.RepeatSubmit;
import com.sun.guardian.repeat.submit.core.enums.client.ClientType;
import com.sun.guardian.repeat.submit.core.enums.scope.KeyScope;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

/**
 * 防重提交规则（注解和 yml 配置的统一抽象）
 *
 * @author scj
 * @since 2026-02-09
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RepeatSubmitRule extends BaseRule {

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

    /**
     * 校验参数合法性，不合法时抛出 IllegalArgumentException
     */
    public void validate(String uri) {
        if (interval <= 0) {
            throw new IllegalArgumentException("[Guardian-Repeat-Submit] interval 必须大于 0，当前值：" + interval + "，URI：" + uri);
        }
    }
}
