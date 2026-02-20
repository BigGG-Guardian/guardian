package com.sun.guardian.slow.api.core.interceptor;

import com.sun.guardian.core.utils.GuardianLogUtils;
import com.sun.guardian.core.utils.MatchUrlRuleUtils;
import com.sun.guardian.slow.api.core.annotation.SlowApiThreshold;
import com.sun.guardian.slow.api.core.statistics.SlowApiStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
    private final SlowApiStatistics statistics;
    private final long globalThreshold;
    private final List<String> excludeUrls;

    /**
     * 构造慢接口检测拦截器
     */
    public SlowApiInterceptor(SlowApiStatistics statistics, long globalThreshold, List<String> excludeUrls) {
        this.statistics = statistics;
        this.globalThreshold = globalThreshold;
        this.excludeUrls = excludeUrls;
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

        if (MatchUrlRuleUtils.matchExcludeUrlRule(excludeUrls, requestUri, pathWithoutContext)) {
            return;
        }

        long threshold = getThreshold(handler);
        if (duration >= threshold) {
            logUtils.slowApiLog(log, request.getMethod(), requestUri, duration, threshold);
            statistics.record(requestUri, duration);
        }
    }

    /**
     * 获取当前接口的慢接口阈值，注解优先于全局配置
     */
    private long getThreshold(Object handler) {
        if (handler instanceof HandlerMethod) {
            SlowApiThreshold annotation = ((HandlerMethod) handler)
                    .getMethodAnnotation(SlowApiThreshold.class);
            if (annotation != null) {
                return annotation.value();
            }
        }
        return globalThreshold;
    }
}
