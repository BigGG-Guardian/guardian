package com.sun.guardian.repeat.submit.core.utils;

import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitExcludeRule;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * URL 规则匹配工具类
 * <p>
 * 提供 yml 配置的防重 URL 规则和排除规则的匹配能力，
 * 支持 AntPath 通配符，自动兼容 context-path。
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 22:29
 */
public class MatchUrlRuleUtils {

    private MatchUrlRuleUtils() {}

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 匹配排除规则（白名单），命中则跳过防重检查
     * <p>
     * 匹配策略：
     * <ol>
     *   <li>先用 servletPath 匹配（处理 pattern 不含 context-path 的情况）</li>
     *   <li>若未命中且存在 context-path，再用 contextPath + servletPath 匹配（处理 pattern 含 context-path 的情况）</li>
     * </ol>
     *
     * @param excludeUrlRules yml 配置的排除规则列表
     * @param contextPath     应用上下文路径（如 /api），无则为空串
     * @param servletPath     请求路径（不含 context-path）
     * @return 匹配到的排除规则，未匹配返回 null
     */
    public static RepeatSubmitExcludeRule matchExcludeUrlRule(List<RepeatSubmitExcludeRule> excludeUrlRules, String contextPath, String servletPath) {
        if (excludeUrlRules == null || excludeUrlRules.isEmpty()) {
            return null;
        }
        boolean hasContextPath = contextPath != null && !contextPath.isEmpty();

        for (RepeatSubmitExcludeRule excludeRule : excludeUrlRules) {
            if (excludeRule.getPattern() == null) {
                continue;
            }
            if (pathMatcher.match(excludeRule.getPattern(), servletPath)) {
                return excludeRule;
            }
            if (hasContextPath && pathMatcher.match(excludeRule.getPattern(), contextPath + servletPath)) {
                return excludeRule;
            }
        }
        return null;
    }

    /**
     * 匹配防重 URL 规则，命中则对该请求执行防重检查
     * <p>
     * 匹配策略：
     * <ol>
     *   <li>先用 servletPath 匹配（处理 pattern 不含 context-path 的情况）</li>
     *   <li>若未命中且存在 context-path，再用 contextPath + servletPath 匹配（处理 pattern 含 context-path 的情况）</li>
     * </ol>
     *
     * @param urlRules    yml 配置的防重 URL 规则列表
     * @param contextPath 应用上下文路径（如 /api），无则为空串
     * @param servletPath 请求路径（不含 context-path）
     * @return 匹配到的防重规则，未匹配返回 null
     */
    public static RepeatSubmitRule matchUrlRule(List<RepeatSubmitRule> urlRules, String contextPath, String servletPath) {
        if (urlRules == null || urlRules.isEmpty()) {
            return null;
        }
        boolean hasContextPath = contextPath != null && !contextPath.isEmpty();

        for (RepeatSubmitRule rule : urlRules) {
            if (rule.getPattern() == null) {
                continue;
            }
            if (pathMatcher.match(rule.getPattern(), servletPath)) {
                return rule;
            }
            if (hasContextPath && pathMatcher.match(rule.getPattern(), contextPath + servletPath)) {
                return rule;
            }
        }
        return null;
    }
}
