package com.sun.guardian.core.interceptor;

import com.sun.guardian.core.annotation.RepeatSubmit;
import com.sun.guardian.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.core.domain.token.RepeatSubmitToken;
import com.sun.guardian.core.exception.RepeatSubmitException;
import com.sun.guardian.core.service.key.KeyGenerator;
import com.sun.guardian.core.service.key.manager.KeyGeneratorManager;
import com.sun.guardian.core.storage.RepeatSubmitStorage;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 防重复提交拦截器
 * 优先匹配 yml 配置的 URL 规则，未命中再检查 @RepeatSubmit 注解
 *
 * @author scj
 * @since 2026-02-09
 */
public class RepeatSubmitInterceptor implements HandlerInterceptor {

    private static final String TOKEN_ATTR = "guardian_repeat_submit_token";

    private final KeyGeneratorManager keyGeneratorManager;
    private final RepeatSubmitStorage repeatSubmitStorage;
    private final List<RepeatSubmitRule> urlRules;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RepeatSubmitInterceptor(KeyGeneratorManager keyGeneratorManager,
                                   RepeatSubmitStorage repeatSubmitStorage,
                                   List<RepeatSubmitRule> urlRules) {
        this.keyGeneratorManager = keyGeneratorManager;
        this.repeatSubmitStorage = repeatSubmitStorage;
        this.urlRules = urlRules;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        RepeatSubmitRule rule = matchUrlRule(request.getContextPath(), request.getServletPath());

        if (rule == null && handler instanceof HandlerMethod) {
            RepeatSubmit annotation = ((HandlerMethod) handler).getMethodAnnotation(RepeatSubmit.class);
            if (annotation != null) {
                rule = RepeatSubmitRule.fromAnnotation(annotation);
            }
        }

        if (rule == null) {
            return true;
        }

        KeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator();
        RepeatSubmitToken token = keyGenerator.generate(rule, request);

        if (!repeatSubmitStorage.tryAcquire(token)) {
            throw new RepeatSubmitException(rule.getMessage());
        }

        request.setAttribute(TOKEN_ATTR, token);
        return true;
    }

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

    /**
     * 从 yml 配置的 URL 规则中匹配当前请求路径
     * <p>同时尝试 servletPath 和 contextPath + servletPath，兼容用户配置带或不带 context-path</p>
     *
     * @param contextPath 应用上下文路径（如 /api），无则为空串
     * @param servletPath 请求路径（不含 context-path）
     * @return 匹配到的规则，未匹配返回 null
     */
    private RepeatSubmitRule matchUrlRule(String contextPath, String servletPath) {
        if (urlRules == null || urlRules.isEmpty()) {
            return null;
        }
        String fullPath = (contextPath != null && !contextPath.isEmpty())
                ? contextPath + servletPath
                : servletPath;

        for (RepeatSubmitRule rule : urlRules) {
            if (rule.getPattern() == null) {
                continue;
            }
            if (pathMatcher.match(rule.getPattern(), servletPath)
                    || pathMatcher.match(rule.getPattern(), fullPath)) {
                return rule;
            }
        }
        return null;
    }
}
