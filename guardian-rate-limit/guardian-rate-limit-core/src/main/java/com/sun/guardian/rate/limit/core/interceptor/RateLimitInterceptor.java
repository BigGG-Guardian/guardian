package com.sun.guardian.rate.limit.core.interceptor;

import com.sun.guardian.core.exception.RateLimitException;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.core.utils.ip.IpUtils;
import com.sun.guardian.core.utils.log.GuardianLogUtils;
import com.sun.guardian.core.utils.match.MatchUrlRuleUtils;
import com.sun.guardian.core.utils.response.ResponseUtils;
import com.sun.guardian.rate.limit.core.annotation.RateLimit;
import com.sun.guardian.rate.limit.core.config.RateLimitConfig;
import com.sun.guardian.rate.limit.core.domain.rule.RateLimitRule;
import com.sun.guardian.rate.limit.core.domain.token.RateLimitToken;
import com.sun.guardian.rate.limit.core.service.key.RateLimitKeyGenerator;
import com.sun.guardian.rate.limit.core.service.response.RateLimitResponseHandler;
import com.sun.guardian.rate.limit.core.statistics.RateLimitStatistics;
import com.sun.guardian.rate.limit.core.storage.RateLimitStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 接口限流拦截器
 * <p>
 * 优先级：exclude-urls 放行 → YAML 规则 → @RateLimit 注解 → 放行
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private static final GuardianLogUtils logUtils = new GuardianLogUtils("[Guardian-Rate-Limit]", "@RateLimit");

    private final RateLimitKeyGenerator keyGenerator;
    private final RateLimitStorage rateLimitStorage;
    private final ResponseUtils responseUtils;
    private final RateLimitConfig rateLimitConfig;
    private final RateLimitStatistics statistics;

    /**
     * 构造限流拦截器
     */
    public RateLimitInterceptor(RateLimitKeyGenerator keyGenerator,
                                RateLimitStorage rateLimitStorage,
                                RateLimitResponseHandler rateLimitResponseHandler,
                                RateLimitConfig rateLimitConfig,
                                RateLimitStatistics statistics,
                                GuardianMessageResolver messageResolver) {
        this.keyGenerator = keyGenerator;
        this.rateLimitStorage = rateLimitStorage;
        this.responseUtils = new ResponseUtils(rateLimitConfig, rateLimitResponseHandler, RateLimitException::new, messageResolver);
        this.rateLimitConfig = rateLimitConfig;
        this.statistics = statistics;
    }

    /**
     * 请求预处理：执行限流判定
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathWithoutContext = MatchUrlRuleUtils.stripContextPath(requestUri, contextPath);
        String ip = IpUtils.getClientIp(request);

        if (MatchUrlRuleUtils.matchExcludeUrlRule(rateLimitConfig.getExcludeUrls(), requestUri, pathWithoutContext)) {
            logUtils.excludeLog(rateLimitConfig.isLogEnabled(), log, requestUri, ip);
            return true;
        }

        RateLimitRule rule = MatchUrlRuleUtils.matchUrlRule(rateLimitConfig.getUrls(), requestUri, pathWithoutContext);
        if (rule != null) {
            logUtils.hitYmlRuleLog(rateLimitConfig.isLogEnabled(), log, requestUri, ip);
        }

        if (rule == null && handler instanceof HandlerMethod) {
            RateLimit annotation = ((HandlerMethod) handler).getMethodAnnotation(RateLimit.class);
            if (annotation != null) {
                rule = RateLimitRule.fromAnnotation(annotation);
                logUtils.hitAnnotationRuleLog(rateLimitConfig.isLogEnabled(), log, requestUri, ip);
            }
        }

        if (rule == null) {
            return true;
        }

        rule.validate(requestUri);
        RateLimitToken token = keyGenerator.generate(rule, request);

        if (!rateLimitStorage.tryAcquire(token)) {
            statistics.recordBlock(requestUri);
            logUtils.blockLog(rateLimitConfig.isLogEnabled(), log, requestUri, token.getKey(), ip);
            return responseUtils.reject(request, response, rule.getMessage());
        }

        statistics.recordPass(requestUri);
        logUtils.passLog(rateLimitConfig.isLogEnabled(), log, requestUri, token.getKey(), ip);

        return true;
    }
}
