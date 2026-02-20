package com.sun.guardian.repeat.submit.core.interceptor;

import cn.hutool.extra.servlet.ServletUtil;
import com.sun.guardian.core.enums.response.ResponseMode;
import com.sun.guardian.core.exception.RepeatSubmitException;
import com.sun.guardian.core.utils.GuardianLogUtils;
import com.sun.guardian.core.utils.MatchUrlRuleUtils;
import com.sun.guardian.core.utils.ResponseUtils;
import com.sun.guardian.repeat.submit.core.annotation.RepeatSubmit;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.repeat.submit.core.domain.token.RepeatSubmitToken;
import com.sun.guardian.repeat.submit.core.service.key.KeyGenerator;
import com.sun.guardian.repeat.submit.core.service.response.RepeatSubmitResponseHandler;
import com.sun.guardian.repeat.submit.core.service.statistics.RepeatSubmitStatistics;
import com.sun.guardian.repeat.submit.core.storage.RepeatSubmitStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 防重复提交拦截器
 * <p>
 * 优先级：排除规则 → YAML 规则 → {@link RepeatSubmit @RepeatSubmit} 注解 → 放行
 *
 * @author scj
 * @since 2026-02-09
 */
public class RepeatSubmitInterceptor implements HandlerInterceptor {

    private static final String TOKEN_ATTR = "guardian_repeat_submit_token";
    private static final Logger log = LoggerFactory.getLogger(RepeatSubmitInterceptor.class);
    private static final GuardianLogUtils logUtils = new GuardianLogUtils("[Guardian-Repeat-Submit]", "@RepeatSubmit");

    private final KeyGenerator keyGenerator;
    private final RepeatSubmitStorage repeatSubmitStorage;
    private final ResponseUtils responseUtils;
    private final List<RepeatSubmitRule> urlRules;
    private final List<String> excludeRules;
    private final boolean logEnable;
    private final RepeatSubmitStatistics statistics;

    public RepeatSubmitInterceptor(KeyGenerator keyGenerator,
                                   RepeatSubmitStorage repeatSubmitStorage,
                                   RepeatSubmitResponseHandler repeatSubmitResponseHandler,
                                   List<RepeatSubmitRule> urlRules,
                                   List<String> excludeRules,
                                   ResponseMode responseMode,
                                   boolean logEnable,
                                   RepeatSubmitStatistics statistics) {
        this.keyGenerator = keyGenerator;
        this.repeatSubmitStorage = repeatSubmitStorage;
        this.responseUtils = new ResponseUtils(responseMode, repeatSubmitResponseHandler, RepeatSubmitException::new);
        this.urlRules = urlRules;
        this.excludeRules = excludeRules;
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

        RepeatSubmitRule rule = MatchUrlRuleUtils.matchUrlRule(urlRules, requestUri, pathWithoutContext);
        if (rule != null) {
            logUtils.hitYmlRuleLog(logEnable, log, requestUri, ip);
        }

        if (rule == null && handler instanceof HandlerMethod) {
            RepeatSubmit annotation = ((HandlerMethod) handler).getMethodAnnotation(RepeatSubmit.class);
            if (annotation != null) {
                rule = RepeatSubmitRule.fromAnnotation(annotation);
                logUtils.hitAnnotationRuleLog(logEnable, log, requestUri, ip);
            }
        }

        if (rule == null) {
            return true;
        }

        rule.validate(requestUri);
        RepeatSubmitToken token = keyGenerator.generate(rule, request);

        if (!repeatSubmitStorage.tryAcquire(token)) {
            statistics.recordBlock(requestUri);
            logUtils.blockLog(logEnable, log, requestUri, token.getKey(), ip);
            return responseUtils.reject(request, response, rule.getMessage());
        }

        statistics.recordPass();
        logUtils.passLog(logEnable, log, requestUri, token.getKey(), ip);
        request.setAttribute(TOKEN_ATTR, token);
        return true;
    }

    /** 异常时释放防重锁 */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (ex != null) {
            RepeatSubmitToken token = (RepeatSubmitToken) request.getAttribute(TOKEN_ATTR);
            if (token != null) {
                repeatSubmitStorage.release(token);
            }
        }
    }
}
