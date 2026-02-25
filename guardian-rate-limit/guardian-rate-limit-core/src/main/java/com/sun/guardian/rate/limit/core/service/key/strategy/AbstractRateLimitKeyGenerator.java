package com.sun.guardian.rate.limit.core.service.key.strategy;

import com.sun.guardian.core.context.UserContext;
import com.sun.guardian.core.utils.ip.IpUtils;
import com.sun.guardian.core.utils.user.UserContextUtils;
import com.sun.guardian.rate.limit.core.domain.key.RateLimitKey;
import com.sun.guardian.rate.limit.core.domain.rule.RateLimitRule;
import com.sun.guardian.rate.limit.core.domain.token.RateLimitToken;
import com.sun.guardian.rate.limit.core.enums.algorithm.RateLimitAlgorithm;
import com.sun.guardian.rate.limit.core.service.key.RateLimitKeyGenerator;

import javax.servlet.http.HttpServletRequest;

import static com.sun.guardian.rate.limit.core.constants.RateLimitKeyPrefixConstants.SW_KEY_PREFIX;
import static com.sun.guardian.rate.limit.core.constants.RateLimitKeyPrefixConstants.TB_KEY_PREFIX;

/**
 * 限流键生成基类（模板方法）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 17:01
 */
public abstract class AbstractRateLimitKeyGenerator implements RateLimitKeyGenerator {

    private final UserContextUtils userContextUtils;

    /**
     * 构造限流键生成基类
     */
    protected AbstractRateLimitKeyGenerator(UserContext userContext) {
        this.userContextUtils = new UserContextUtils(userContext);
    }

    /** 生成限流令牌 */
    @Override
    public RateLimitToken generate(RateLimitRule rule, HttpServletRequest request) {
        RateLimitKey rateLimitKey = buildRateLimitKey(rule, request);
        String key = buildKey(rateLimitKey);

        String prefix = rule.getAlgorithm() == RateLimitAlgorithm.TOKEN_BUCKET ? TB_KEY_PREFIX : SW_KEY_PREFIX;
        String finishKey = String.format(prefix, key);

        return new RateLimitToken()
                .setKey(finishKey)
                .setQps(rule.getQps())
                .setWindow(rule.getWindow())
                .setWindowUnit(rule.getWindowUnit())
                .setAlgorithm(rule.getAlgorithm())
                .setCapacity(rule.getCapacity());
    }

    /** 组装限流键数据 */
    private RateLimitKey buildRateLimitKey(RateLimitRule rule, HttpServletRequest request) {
        return new RateLimitKey()
                .setServletUri(request.getServletPath())
                .setMethod(request.getMethod())
                .setUserId(userContextUtils.resolveUserId(request))
                .setClientIp(IpUtils.getClientIp(request))
                .setKeyScope(rule.getRateLimitScope().key);
    }

    /** 子类实现：拼接限流 Key */
    protected abstract String buildKey(RateLimitKey rateLimitKey);
}
