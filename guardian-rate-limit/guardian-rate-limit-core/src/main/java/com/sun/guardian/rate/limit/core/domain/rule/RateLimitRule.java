package com.sun.guardian.rate.limit.core.domain.rule;

import com.sun.guardian.core.domain.BaseRule;
import com.sun.guardian.rate.limit.core.annotation.RateLimit;
import com.sun.guardian.rate.limit.core.enums.algorithm.RateLimitAlgorithm;
import com.sun.guardian.rate.limit.core.enums.scope.RateLimitKeyScope;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

/**
 * 接口限流规则（注解和 yml 配置的统一抽象）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 16:23
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RateLimitRule extends BaseRule {

    private int qps = 10;
    private int window = 1;
    private TimeUnit windowUnit = TimeUnit.SECONDS;
    private String message = "请求过于频繁，请稍后再试";
    private RateLimitKeyScope rateLimitScope = RateLimitKeyScope.GLOBAL;
    private RateLimitAlgorithm algorithm = RateLimitAlgorithm.SLIDING_WINDOW;
    /** 令牌桶容量，{@code <= 0} 时取 qps */
    private int capacity = -1;

    /** 从注解创建规则 */
    public static RateLimitRule fromAnnotation(RateLimit annotation) {
        return new RateLimitRule()
                .setQps(annotation.qps())
                .setWindow(annotation.window())
                .setWindowUnit(annotation.windowUnit())
                .setMessage(annotation.message())
                .setRateLimitScope(annotation.rateLimitScope())
                .setAlgorithm(annotation.algorithm())
                .setCapacity(annotation.capacity());
    }
}
