package com.sun.guardian.rate.limit.core.interceptor;

import cn.hutool.extra.servlet.ServletUtil;
import com.sun.guardian.core.enums.response.ResponseMode;
import com.sun.guardian.core.exception.RateLimitException;
import com.sun.guardian.core.utils.GuardianLogUtils;
import com.sun.guardian.core.utils.MatchUrlRuleUtils;
import com.sun.guardian.rate.limit.core.annotation.RateLimit;
import com.sun.guardian.rate.limit.core.domain.rule.RateLimitRule;
import com.sun.guardian.rate.limit.core.domain.token.RateLimitToken;
import com.sun.guardian.rate.limit.core.service.key.RateLimitKeyGenerator;
import com.sun.guardian.rate.limit.core.service.key.manager.RateLimitKeyGeneratorManager;
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
import java.util.List;

/**
 * 接口限流拦截器
 * <p>
 * 优先级：排除规则 → YAML 规则 → {@link RateLimit @RateLimit} 注解 → 放行
 *
 * @author scj
 * @since 2026-02-09
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private static final GuardianLogUtils logUtils = new GuardianLogUtils("[Guardian-Rate-Limit]", "@RateLimit");

    private final RateLimitKeyGeneratorManager keyGeneratorManager;
    private final RateLimitStorage rateLimitStorage;
    private final RateLimitResponseHandler rateLimitResponseHandler;
    private final List<RateLimitRule> urlRules;
    private final List<String> excludeRules;
    private final ResponseMode responseMode;
    private final boolean logEnable;
    private final RateLimitStatistics statistics;
    public RateLimitInterceptor(RateLimitKeyGeneratorManager keyGeneratorManager,
                                RateLimitStorage rateLimitStorage,
                                RateLimitResponseHandler rateLimitResponseHandler,
                                List<RateLimitRule> urlRules,
                                List<String> excludeRules,
                                ResponseMode responseMode,
                                boolean logEnable,
                                RateLimitStatistics statistics) {
        this.keyGeneratorManager = keyGeneratorManager;
        this.rateLimitStorage = rateLimitStorage;
        this.rateLimitResponseHandler = rateLimitResponseHandler;
        this.urlRules = urlRules;
        this.excludeRules = excludeRules;
        this.responseMode = responseMode;
        this.logEnable = logEnable;
        this.statistics = statistics;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathWithoutContext = MatchUrlRuleUtils.stripContextPath(requestUri, contextPath);
        String ip = ServletUtil.getClientIP(request);

        if (MatchUrlRuleUtils.matchExcludeUrlRule(excludeRules, requestUri, pathWithoutContext)) {
            logUtils.excludeLog(logEnable, log, requestUri, ip);
            return true;
        }

        RateLimitRule rule = MatchUrlRuleUtils.matchUrlRule(urlRules, requestUri, pathWithoutContext);
        if (rule != null) {
            logUtils.hitYmlRuleLog(logEnable, log, requestUri, ip);
        }

        if (rule == null && handler instanceof HandlerMethod) {
            RateLimit annotation = ((HandlerMethod) handler).getMethodAnnotation(RateLimit.class);
            if (annotation != null) {
                rule = RateLimitRule.fromAnnotation(annotation);
                logUtils.hitAnnotationRuleLog(logEnable, log, requestUri, ip);
            }
        }

        if (rule == null) {
            return true;
        }

        RateLimitKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator();
        RateLimitToken token = keyGenerator.generate(rule, request);

        if (!rateLimitStorage.tryAcquire(token)) {
            statistics.recordBlock(requestUri);
            logUtils.blockLog(logEnable, log, requestUri, token.getKey(), ip);
            if (responseMode == ResponseMode.JSON) {
                rateLimitResponseHandler.handle(request, response, rule.getMessage());
                return false;
            }
            throw new RateLimitException(rule.getMessage());
        }

        statistics.recordPass(requestUri);
        logUtils.passLog(logEnable, log, requestUri, token.getKey(), ip);

        return true;
    }
}
