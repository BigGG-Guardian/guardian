package com.sun.guardian.repeat.submit.core.interceptor;

import com.sun.guardian.core.exception.RepeatSubmitException;
import com.sun.guardian.repeat.submit.core.annotation.RepeatSubmit;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitExcludeRule;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.repeat.submit.core.domain.token.RepeatSubmitToken;
import com.sun.guardian.repeat.submit.core.service.key.KeyGenerator;
import com.sun.guardian.repeat.submit.core.service.key.manager.KeyGeneratorManager;
import com.sun.guardian.repeat.submit.core.storage.RepeatSubmitStorage;
import com.sun.guardian.repeat.submit.core.utils.MatchUrlRuleUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 防重复提交拦截器
 * <p>
 * 拦截判定优先级（从高到低）：
 * <ol>
 *   <li>排除规则（白名单）— 命中直接放行，跳过所有防重检查</li>
 *   <li>YAML URL 规则 — 命中则按规则执行防重检查</li>
 *   <li>{@link RepeatSubmit @RepeatSubmit} 注解 — 作为兜底，YAML 未命中时生效</li>
 *   <li>均未命中 — 直接放行</li>
 * </ol>
 *
 * @author scj
 * @since 2026-02-09
 */
public class RepeatSubmitInterceptor implements HandlerInterceptor {

    /**
     * 存储在 request attribute 中的 Token key，用于 afterCompletion 阶段释放锁
     */
    private static final String TOKEN_ATTR = "guardian_repeat_submit_token";

    private final KeyGeneratorManager keyGeneratorManager;
    private final RepeatSubmitStorage repeatSubmitStorage;
    private final List<RepeatSubmitRule> urlRules;
    private final List<RepeatSubmitExcludeRule> excludeRules;

    public RepeatSubmitInterceptor(KeyGeneratorManager keyGeneratorManager,
                                   RepeatSubmitStorage repeatSubmitStorage,
                                   List<RepeatSubmitRule> urlRules,
                                   List<RepeatSubmitExcludeRule> excludeRules) {
        this.keyGeneratorManager = keyGeneratorManager;
        this.repeatSubmitStorage = repeatSubmitStorage;
        this.urlRules = urlRules;
        this.excludeRules = excludeRules;
    }

    /**
     * 请求前置处理：按优先级匹配规则，执行防重检查
     *
     * @throws RepeatSubmitException 重复提交时抛出，由业务全局异常处理器捕获
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        if (MatchUrlRuleUtils.matchExcludeUrlRule(excludeRules, request.getContextPath(), request.getServletPath()) != null) {
            return true;
        }

        RepeatSubmitRule rule = MatchUrlRuleUtils.matchUrlRule(urlRules, request.getContextPath(), request.getServletPath());

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

    /**
     * 请求完成后处理：业务异常时自动释放防重锁，避免异常后锁未释放导致后续请求被误拦
     */
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
