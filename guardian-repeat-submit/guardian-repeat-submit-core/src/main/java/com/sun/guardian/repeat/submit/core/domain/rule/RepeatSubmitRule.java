package com.sun.guardian.repeat.submit.core.domain.rule;

import com.sun.guardian.core.domain.BaseRule;
import com.sun.guardian.repeat.submit.core.annotation.RepeatSubmit;
import com.sun.guardian.repeat.submit.core.config.RepeatSubmitConfig;
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
 * @version java version 1.8
 * @since 2026-02-09
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RepeatSubmitRule extends BaseRule {

    public static final int ANNOTATION_DEFAULT_INTERVAL = 5;
    public static final TimeUnit ANNOTATION_DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    public static final String ANNOTATION_DEFAULT_MESSAGE = "您的请求过于频繁，请稍后再试";
    public static final KeyScope ANNOTATION_DEFAULT_KEY_SCOPE = KeyScope.USER;
    public static final ClientType ANNOTATION_DEFAULT_CLIENT_TYPE = ClientType.PC;

    /**
     * 防重时间间隔
     */
    private int interval = ANNOTATION_DEFAULT_INTERVAL;

    /**
     * 时间单位
     */
    private TimeUnit timeUnit = ANNOTATION_DEFAULT_TIME_UNIT;

    /**
     * 错误提示信息
     */
    private String message = ANNOTATION_DEFAULT_MESSAGE;

    /**
     * 防重键维度
     */
    private KeyScope keyScope = ANNOTATION_DEFAULT_KEY_SCOPE;

    /**
     * 客户端类型
     */
    private ClientType clientType = ANNOTATION_DEFAULT_CLIENT_TYPE;

    /**
     * 从注解创建规则，注解未显式指定的字段使用 Properties 全局默认值（支持动态刷新）
     *
     * @param annotation 防重注解
     * @param config     防重配置（持有动态默认值）
     * @return 合并后的防重规则
     */
    public static RepeatSubmitRule fromAnnotation(RepeatSubmit annotation, RepeatSubmitConfig config) {
        return new RepeatSubmitRule()
                .setInterval(annotation.interval() != ANNOTATION_DEFAULT_INTERVAL ? annotation.interval() : config.getInterval())
                .setTimeUnit(annotation.timeUnit() != ANNOTATION_DEFAULT_TIME_UNIT ? annotation.timeUnit() : config.getTimeUnit())
                .setMessage(!annotation.message().equals(ANNOTATION_DEFAULT_MESSAGE) ? annotation.message() : config.getMessage())
                .setKeyScope(annotation.keyScope() != ANNOTATION_DEFAULT_KEY_SCOPE ? annotation.keyScope() : config.getKeyScope())
                .setClientType(annotation.clientType() != ANNOTATION_DEFAULT_CLIENT_TYPE ? annotation.clientType() : config.getClientType());
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
