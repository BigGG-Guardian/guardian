package com.sun.guardian.slow.api.core.interceptor;

import com.sun.guardian.core.utils.log.GuardianLogUtils;
import com.sun.guardian.core.utils.match.MatchUrlRuleUtils;
import com.sun.guardian.slow.api.core.annotation.SlowApiThreshold;
import com.sun.guardian.slow.api.core.config.SlowApiConfig;
import com.sun.guardian.slow.api.core.statistics.SlowApiStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 慢接口检测拦截器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 18:24
 */
public class SlowApiInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(SlowApiInterceptor.class);
    private static final String START_TIME_ATTR = "guardian.slow-api.startTime";
    private static final GuardianLogUtils logUtils = new GuardianLogUtils("[Guardian-Slow-Api]", "@SlowApiThreshold");
    private final SlowApiConfig slowApiConfig;
    private final SlowApiStatistics statistics;

    /**
     * 构造慢接口检测拦截器
     */
    public SlowApiInterceptor(SlowApiConfig slowApiConfig, SlowApiStatistics statistics) {
        this.slowApiConfig = slowApiConfig;
        this.statistics = statistics;
    }

    /**
     * 请求进入时记录开始时间
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    /**
     * 请求完成后计算耗时，超过阈值则记录慢接口
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) return;

        long duration = System.currentTimeMillis() - startTime;

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathWithoutContext = MatchUrlRuleUtils.stripContextPath(requestUri, contextPath);

        if (MatchUrlRuleUtils.matchExcludeUrlRule(slowApiConfig.getExcludeUrls(), requestUri, pathWithoutContext)) {
            return;
        }

        long threshold;
        if (handler instanceof HandlerMethod) {
            SlowApiThreshold annotation = ((HandlerMethod) handler).getMethodAnnotation(SlowApiThreshold.class);
            threshold = (annotation != null) ? annotation.value() : slowApiConfig.getThreshold();
        } else {
            threshold = slowApiConfig.getThreshold();
        }

        if (duration >= threshold) {
            logUtils.slowApiLog(log, request.getMethod(), requestUri, duration, threshold);
            statistics.record(requestUri, duration);
        }
    }

}
