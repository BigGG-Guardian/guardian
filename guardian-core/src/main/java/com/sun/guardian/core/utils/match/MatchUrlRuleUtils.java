package com.sun.guardian.core.utils.match;

import com.sun.guardian.core.domain.BaseRule;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * URL 规则匹配工具类（AntPath 通配符，兼容 context-path）
 * 每条规则先用完整 requestUri 匹配，未命中再用去掉 context-path 后的路径匹配。
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
     * 去除 context-path 前缀
     */
    public static String stripContextPath(String requestUri, String contextPath) {
        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    /**
     * 匹配排除规则（白名单），命中则跳过
     */
    public static boolean matchExcludeUrlRule(List<String> excludeUrlRules,
                                              String requestUri, String pathWithoutContext) {
        if (excludeUrlRules == null || excludeUrlRules.isEmpty()) {
            return false;
        }
        for (String excludeRule : excludeUrlRules) {
            if (excludeRule == null || excludeRule.trim().isEmpty()) {
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
     * 匹配 URL 规则，命中返回对应规则
     */
    public static <T extends BaseRule> T matchUrlRule(List<T> urlRules,
                                                      String requestUri, String pathWithoutContext) {
        if (urlRules == null || urlRules.isEmpty()) {
            return null;
        }
        for (T rule : urlRules) {
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

    /**
     * 匹配 URL 规则，命中返回对应URL
     */
    public static String matchUrl(Set<String> urlPattern,
                                  String requestUri, String pathWithoutContext) {
        if (urlPattern == null || urlPattern.isEmpty()) {
            return null;
        }
        for (String url : urlPattern) {
            if (!StringUtils.hasText(url)) {
                continue;
            }
            if (pathMatcher.match(url, requestUri)) {
                return url;
            }
            if (!requestUri.equals(pathWithoutContext)
                    && pathMatcher.match(url, pathWithoutContext)) {
                return url;
            }
        }
        return null;
    }
}
