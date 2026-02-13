package com.sun.guardian.repeat.submit.core.utils;

import cn.hutool.core.util.StrUtil;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * URL 规则匹配工具类
 * <p>
 * 提供 yml 配置的防重 URL 规则和排除规则的匹配能力，
 * 支持 AntPath 通配符，自动兼容 context-path。
 * <p>
 * 匹配策略（对每条规则依次尝试）：
 * <ol>
 *   <li>先用完整 requestUri 匹配（兼容 pattern 包含 context-path / 路径前缀的情况）</li>
 *   <li>若未命中且两者不同，再用去除 context-path 后的路径匹配（兼容 pattern 不含 context-path 的情况）</li>
 * </ol>
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 22:29
 */
public class MatchUrlRuleUtils {

    private MatchUrlRuleUtils() {
    }

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 从 requestURI 中去除 context-path 前缀
     *
     * @param requestUri  完整请求路径，如 /admin-api/system/auth/login
     * @param contextPath 应用上下文路径，如 /admin-api，无则为空串
     * @return 去除 context-path 后的路径，如 /system/auth/login；无 context-path 时与 requestUri 相同
     */
    public static String stripContextPath(String requestUri, String contextPath) {
        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    /**
     * 匹配排除规则（白名单），命中则跳过防重检查
     *
     * @param excludeUrlRules    yml 配置的排除规则列表
     * @param requestUri         完整请求路径，如 /admin-api/system/auth/login
     * @param pathWithoutContext 去除 context-path 后的路径，如 /system/auth/login；无 context-path 时与 requestUri 相同
     * @return true-放行 false-不放行
     */
    public static boolean matchExcludeUrlRule(List<String> excludeUrlRules,
                                              String requestUri, String pathWithoutContext) {
        if (excludeUrlRules == null || excludeUrlRules.isEmpty()) {
            return false;
        }
        for (String excludeRule : excludeUrlRules) {
            if (StrUtil.isBlank(excludeRule)) {
                continue;
            }
            if (pathMatcher.match(excludeRule, requestUri)) {
                return true;
            }
            if (!requestUri.equals(pathWithoutContext)
                    && pathMatcher.match(excludeRule, pathWithoutContext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 匹配防重 URL 规则，命中则对该请求执行防重检查
     *
     * @param urlRules           yml 配置的防重 URL 规则列表
     * @param requestUri         完整请求路径，如 /admin-api/system/auth/login
     * @param pathWithoutContext 去除 context-path 后的路径，如 /system/auth/login；无 context-path 时与 requestUri 相同
     * @return 匹配到的防重规则，未匹配返回 null
     */
    public static RepeatSubmitRule matchUrlRule(List<RepeatSubmitRule> urlRules,
                                                String requestUri, String pathWithoutContext) {
        if (urlRules == null || urlRules.isEmpty()) {
            return null;
        }
        for (RepeatSubmitRule rule : urlRules) {
            if (rule.getPattern() == null) {
                continue;
            }
            if (pathMatcher.match(rule.getPattern(), requestUri)) {
                return rule;
            }
            if (!requestUri.equals(pathWithoutContext)
                    && pathMatcher.match(rule.getPattern(), pathWithoutContext)) {
                return rule;
            }
        }
        return null;
    }
}
