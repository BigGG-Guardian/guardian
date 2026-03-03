package com.sun.guardian.sign.core.interceptor;

import com.sun.guardian.core.exception.SignVerifyException;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.guardian.core.utils.args.ArgsUtils;
import com.sun.guardian.core.utils.ip.IpUtils;
import com.sun.guardian.core.utils.json.GuardianJsonUtils;
import com.sun.guardian.core.utils.log.GuardianLogUtils;
import com.sun.guardian.core.utils.match.MatchUrlRuleUtils;
import com.sun.guardian.core.utils.response.ResponseUtils;
import com.sun.guardian.core.wrapper.RepeatableRequestWrapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.SortedMap;
import com.sun.guardian.sign.core.advice.SignResultSignAdvice;
import com.sun.guardian.sign.core.annotation.SignVerify;
import com.sun.guardian.sign.core.config.SignConfig;
import com.sun.guardian.sign.core.domain.rule.SignRule;
import com.sun.guardian.sign.core.service.response.SignResponseHandler;
import com.sun.guardian.sign.core.service.sign.SignService;
import com.sun.guardian.sign.core.statistics.SignStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 参数签名拦截器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-02 21:49
 */
public class SignVerifyInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(SignVerifyInterceptor.class);
    private static final GuardianLogUtils logUtils = new GuardianLogUtils("[Guardian-Sign]", "@SignVerify");

    private final SignConfig signConfig;
    private final SignService signService;
    private final SignStatistics statistics;
    private final ResponseUtils responseUtils;

    public SignVerifyInterceptor(SignConfig signConfig, SignService signService, SignResponseHandler responseHandler, SignStatistics statistics, GuardianMessageResolver messageResolver) {
        this.signConfig = signConfig;
        this.signService = signService;
        this.statistics = statistics;
        this.responseUtils = new ResponseUtils(signConfig, responseHandler, SignVerifyException::new, messageResolver);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathWithoutContext = MatchUrlRuleUtils.stripContextPath(requestUri, contextPath);
        String ip = IpUtils.getClientIp(request);

        SignRule rule = MatchUrlRuleUtils.matchUrlRule(signConfig.getUrls(), requestUri, pathWithoutContext);
        if (rule != null) {
            logUtils.hitYmlRuleLog(signConfig.isLogEnabled(), log, requestUri, ip);
        }

        if (rule == null && handler instanceof HandlerMethod) {
            SignVerify annotation = ((HandlerMethod) handler).getMethodAnnotation(SignVerify.class);
            if (annotation != null) {
                rule = SignRule.fromAnnotation(annotation);
                logUtils.hitAnnotationRuleLog(signConfig.isLogEnabled(), log, requestUri, ip);
            }
        }

        if (rule == null) {
            return true;
        }

        String timestampStr = request.getHeader(signConfig.getTimestampHeader());
        if (!StringUtils.hasText(timestampStr)) {
            statistics.recordBlock(requestUri);
            logUtils.blockLog(signConfig.isLogEnabled(), log, requestUri, ip);
            responseUtils.reject(request, response, signConfig.getMissingTimestampMessage());
            return false;
        }

        long timestamp = Long.parseLong(timestampStr);
        long now = System.currentTimeMillis();
        if (signConfig.getMaxAge() > 0 && Math.abs(now - timestamp) > signConfig.getMaxAgeUnit().toMillis(signConfig.getMaxAge())) {
            statistics.recordBlock(requestUri);
            logUtils.blockLog(signConfig.isLogEnabled(), log, requestUri, ip);
            responseUtils.reject(request, response, signConfig.getExpiredMessage());
            return false;
        }

        String sign = request.getHeader(signConfig.getSignHeader());
        if (!StringUtils.hasText(sign)) {
            statistics.recordBlock(requestUri);
            logUtils.blockLog(signConfig.isLogEnabled(), log, requestUri, ip);
            responseUtils.reject(request, response, signConfig.getMissingSignMessage());
            return false;
        }

        String genSign = signService.sign(ArgsUtils.toSorted(request), String.valueOf(timestamp), signConfig.getSecretKey(), rule.getAlgorithm());
        if (!Objects.equals(sign, genSign)) {
            statistics.recordBlock(requestUri);
            logUtils.blockLog(signConfig.isLogEnabled(), log, requestUri, ip);
            responseUtils.reject(request, response, rule.getSignVerifyMessage());
            return false;
        }

        statistics.recordPass();
        logUtils.passLog(signConfig.isLogEnabled(), log, requestUri, ip);
        if (signConfig.isResultSign()) {
            request.setAttribute(SignResultSignAdvice.RESPONSE_SIGN_ALGORITHM_ATTRIBUTE, rule.getAlgorithm());
        }

        return true;
    }
}
