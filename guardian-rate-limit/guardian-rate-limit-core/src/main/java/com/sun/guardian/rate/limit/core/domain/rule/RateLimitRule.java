package com.sun.guardian.rate.limit.core.domain.rule;

import com.sun.guardian.core.domain.BaseRule;
import com.sun.guardian.rate.limit.core.annotation.RateLimit;
import com.sun.guardian.rate.limit.core.config.RateLimitConfig;
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

    public static final int ANNOTATION_DEFAULT_QPS = 10;
    public static final int ANNOTATION_DEFAULT_WINDOW = 1;
    public static final TimeUnit ANNOTATION_DEFAULT_WINDOW_UNIT = TimeUnit.SECONDS;
    public static final String ANNOTATION_DEFAULT_MESSAGE = "请求过于频繁，请稍后再试";
    public static final RateLimitKeyScope ANNOTATION_DEFAULT_SCOPE = RateLimitKeyScope.GLOBAL;
    public static final RateLimitAlgorithm ANNOTATION_DEFAULT_ALGORITHM = RateLimitAlgorithm.SLIDING_WINDOW;
    public static final int ANNOTATION_DEFAULT_CAPACITY = -1;

    private int qps = ANNOTATION_DEFAULT_QPS;
    private int window = ANNOTATION_DEFAULT_WINDOW;
    private TimeUnit windowUnit = ANNOTATION_DEFAULT_WINDOW_UNIT;
    private String message = ANNOTATION_DEFAULT_MESSAGE;
    private RateLimitKeyScope rateLimitScope = ANNOTATION_DEFAULT_SCOPE;
    private RateLimitAlgorithm algorithm = ANNOTATION_DEFAULT_ALGORITHM;
    /**
     * 令牌桶容量，<= 0 时取 qps
     */
    private int capacity = ANNOTATION_DEFAULT_CAPACITY;

    /**
     * 从注解创建规则，注解未显式指定的字段使用 Properties 全局默认值（支持动态刷新）
     *
     * @param annotation 限流注解
     * @param config     限流配置（持有动态默认值）
     * @return 合并后的限流规则
     */
    public static RateLimitRule fromAnnotation(RateLimit annotation, RateLimitConfig config) {
        return new RateLimitRule()
                .setQps(annotation.qps() != ANNOTATION_DEFAULT_QPS ? annotation.qps() : config.getQps())
                .setWindow(annotation.window() != ANNOTATION_DEFAULT_WINDOW ? annotation.window() : config.getWindow())
                .setWindowUnit(annotation.windowUnit() != ANNOTATION_DEFAULT_WINDOW_UNIT ? annotation.windowUnit() : config.getWindowUnit())
                .setMessage(!annotation.message().equals(ANNOTATION_DEFAULT_MESSAGE) ? annotation.message() : config.getMessage())
                .setRateLimitScope(annotation.rateLimitScope() != ANNOTATION_DEFAULT_SCOPE ? annotation.rateLimitScope() : config.getRateLimitScope())
                .setAlgorithm(annotation.algorithm() != ANNOTATION_DEFAULT_ALGORITHM ? annotation.algorithm() : config.getAlgorithm())
                .setCapacity(annotation.capacity() != ANNOTATION_DEFAULT_CAPACITY ? annotation.capacity() : config.getCapacity());
    }

    /**
     * 校验参数合法性，不合法时抛出 IllegalArgumentException
     */
    public void validate(String uri) {
        if (qps <= 0) {
            throw new IllegalArgumentException("[Guardian-Rate-Limit] qps 必须大于 0，当前值：" + qps + "，URI：" + uri);
        }
        if (window <= 0) {
            throw new IllegalArgumentException("[Guardian-Rate-Limit] window 必须大于 0，当前值：" + window + "，URI：" + uri);
        }
    }
}
