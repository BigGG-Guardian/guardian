package com.sun.guardian.repeat.submit.core.interceptor;

import com.sun.guardian.core.exception.RepeatSubmitException;
import com.sun.guardian.repeat.submit.core.annotation.RepeatSubmit;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.repeat.submit.core.domain.token.RepeatSubmitToken;
import com.sun.guardian.repeat.submit.core.enums.response.ResponseMode;
import com.sun.guardian.repeat.submit.core.service.key.KeyGenerator;
import com.sun.guardian.repeat.submit.core.service.key.manager.KeyGeneratorManager;
import com.sun.guardian.repeat.submit.core.service.response.RepeatSubmitResponseHandler;
import com.sun.guardian.repeat.submit.core.storage.RepeatSubmitStorage;
import com.sun.guardian.repeat.submit.core.utils.MatchUrlRuleUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
 * <p>
 * 重复提交时的响应行为由 {@link ResponseMode} 决定：
 * <ul>
 *   <li>{@link ResponseMode#EXCEPTION EXCEPTION}（默认）— 抛出 {@link RepeatSubmitException}，由业务全局异常处理器捕获</li>
 *   <li>{@link ResponseMode#JSON JSON} — 由 {@link RepeatSubmitResponseHandler} 直接写入 JSON 响应</li>
 * </ul>
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
    private final RepeatSubmitResponseHandler repeatSubmitResponseHandler;
    private final List<RepeatSubmitRule> urlRules;
    private final List<String> excludeRules;
    private final ResponseMode responseMode;

    public RepeatSubmitInterceptor(KeyGeneratorManager keyGeneratorManager,
                                   RepeatSubmitStorage repeatSubmitStorage,
                                   RepeatSubmitResponseHandler repeatSubmitResponseHandler,
                                   List<RepeatSubmitRule> urlRules,
                                   List<String> excludeRules,
                                   ResponseMode responseMode) {
        this.keyGeneratorManager = keyGeneratorManager;
        this.repeatSubmitStorage = repeatSubmitStorage;
        this.repeatSubmitResponseHandler = repeatSubmitResponseHandler;
        this.urlRules = urlRules;
        this.excludeRules = excludeRules;
        this.responseMode = responseMode;
    }

    /**
     * 请求前置处理：按优先级匹配规则，执行防重检查
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler  目标处理器（Controller 方法）
     * @return {@code true} 放行，{@code false} 拦截（仅 JSON 响应模式）
     * @throws RepeatSubmitException 异常响应模式下，重复提交时抛出
     * @throws IOException           JSON 响应模式下，写入响应时可能抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathWithoutContext = MatchUrlRuleUtils.stripContextPath(requestUri, contextPath);

        if (MatchUrlRuleUtils.matchExcludeUrlRule(excludeRules, requestUri, pathWithoutContext)) {
            return true;
        }

        RepeatSubmitRule rule = MatchUrlRuleUtils.matchUrlRule(urlRules, requestUri, pathWithoutContext);

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
            if (responseMode == ResponseMode.JSON) {
                repeatSubmitResponseHandler.handle(request, response, rule.getMessage());
                return false;
            }
            throw new RepeatSubmitException(rule.getMessage());
        }

        request.setAttribute(TOKEN_ATTR, token);
        return true;
    }

    /**
     * 请求完成后处理：业务异常时自动释放防重锁，避免异常后锁未释放导致后续请求被误拦
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler  目标处理器
     * @param ex       业务执行期间抛出的异常，正常完成时为 null
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
