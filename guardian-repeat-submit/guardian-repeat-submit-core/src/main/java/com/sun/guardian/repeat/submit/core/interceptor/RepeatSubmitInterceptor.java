package com.sun.guardian.repeat.submit.core.interceptor;

import cn.hutool.extra.servlet.ServletUtil;
import com.sun.guardian.core.exception.RepeatSubmitException;
import com.sun.guardian.repeat.submit.core.annotation.RepeatSubmit;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.repeat.submit.core.domain.token.RepeatSubmitToken;
import com.sun.guardian.repeat.submit.core.enums.response.ResponseMode;
import com.sun.guardian.repeat.submit.core.service.key.KeyGenerator;
import com.sun.guardian.repeat.submit.core.service.key.manager.KeyGeneratorManager;
import com.sun.guardian.repeat.submit.core.service.response.RepeatSubmitResponseHandler;
import com.sun.guardian.repeat.submit.core.service.statistics.RepeatSubmitStatistics;
import com.sun.guardian.repeat.submit.core.storage.RepeatSubmitStorage;
import com.sun.guardian.repeat.submit.core.utils.LogUtils;
import com.sun.guardian.repeat.submit.core.utils.MatchUrlRuleUtils;
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

    private static final Logger log = LoggerFactory.getLogger(RepeatSubmitInterceptor.class);

    /**
     * 防重键生成管理器
     */
    private final KeyGeneratorManager keyGeneratorManager;
    /**
     * 防重存储（Redis / Local）
     */
    private final RepeatSubmitStorage repeatSubmitStorage;
    /**
     * JSON 响应处理器（仅 {@link ResponseMode#JSON} 模式使用）
     */
    private final RepeatSubmitResponseHandler repeatSubmitResponseHandler;
    /**
     * YAML 配置的 URL 防重规则列表
     */
    private final List<RepeatSubmitRule> urlRules;
    /**
     * 排除规则（白名单）列表
     */
    private final List<String> excludeRules;
    /**
     * 响应模式：抛异常 / 直接返回 JSON
     */
    private final ResponseMode responseMode;
    /**
     * 是否开启拦截日志
     */
    private final boolean logEnable;
    /**
     * 拦截统计（内存，运行时监控）
     */
    private final RepeatSubmitStatistics statistics;

    /**
     * 构造防重复提交拦截器
     *
     * @param keyGeneratorManager         防重键生成管理器
     * @param repeatSubmitStorage         防重存储实现
     * @param repeatSubmitResponseHandler JSON 响应处理器
     * @param urlRules                    YAML 配置的 URL 防重规则
     * @param excludeRules                排除规则（白名单）
     * @param responseMode                响应模式
     * @param logEnable                   是否开启拦截日志
     * @param statistics                  拦截统计
     */
    public RepeatSubmitInterceptor(KeyGeneratorManager keyGeneratorManager,
                                   RepeatSubmitStorage repeatSubmitStorage,
                                   RepeatSubmitResponseHandler repeatSubmitResponseHandler,
                                   List<RepeatSubmitRule> urlRules,
                                   List<String> excludeRules,
                                   ResponseMode responseMode,
                                   boolean logEnable,
                                   RepeatSubmitStatistics statistics) {
        this.keyGeneratorManager = keyGeneratorManager;
        this.repeatSubmitStorage = repeatSubmitStorage;
        this.repeatSubmitResponseHandler = repeatSubmitResponseHandler;
        this.urlRules = urlRules;
        this.excludeRules = excludeRules;
        this.responseMode = responseMode;
        this.logEnable = logEnable;
        this.statistics = statistics;
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
        String ip = ServletUtil.getClientIP(request);

        if (MatchUrlRuleUtils.matchExcludeUrlRule(excludeRules, requestUri, pathWithoutContext)) {
            LogUtils.excludeLog(logEnable, log, requestUri, ip);
            return true;
        }

        RepeatSubmitRule rule = MatchUrlRuleUtils.matchUrlRule(urlRules, requestUri, pathWithoutContext);
        if (rule != null) {
            LogUtils.hitYmlRuleLog(logEnable, log, requestUri, ip);
        }

        if (rule == null && handler instanceof HandlerMethod) {
            RepeatSubmit annotation = ((HandlerMethod) handler).getMethodAnnotation(RepeatSubmit.class);
            if (annotation != null) {
                rule = RepeatSubmitRule.fromAnnotation(annotation);
                LogUtils.hitAnnotationRuleLog(logEnable, log, requestUri, ip);
            }
        }

        if (rule == null) {
            return true;
        }

        KeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator();
        RepeatSubmitToken token = keyGenerator.generate(rule, request);

        if (!repeatSubmitStorage.tryAcquire(token)) {
            statistics.recordBlock(requestUri);
            LogUtils.repeatSubmitLog(logEnable, log, requestUri, token.getKey(), ip);
            if (responseMode == ResponseMode.JSON) {
                repeatSubmitResponseHandler.handle(request, response, rule.getMessage());
                return false;
            }
            throw new RepeatSubmitException(rule.getMessage());
        }

        statistics.recordPass();
        LogUtils.passLog(logEnable, log, requestUri, token.getKey(), ip);
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
