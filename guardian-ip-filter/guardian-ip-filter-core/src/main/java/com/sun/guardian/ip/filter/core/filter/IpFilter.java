package com.sun.guardian.ip.filter.core.filter;

import cn.hutool.extra.servlet.ServletUtil;
import com.sun.guardian.core.exception.IpForbiddenException;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.core.utils.GuardianLogUtils;
import com.sun.guardian.core.utils.IpMatcher;
import com.sun.guardian.core.utils.MatchUrlRuleUtils;
import com.sun.guardian.core.utils.ResponseUtils;
import com.sun.guardian.ip.filter.core.config.IpFilterConfig;
import com.sun.guardian.ip.filter.core.domain.rule.UrlWhiteRule;
import com.sun.guardian.ip.filter.core.service.response.IpFilterResponseHandler;
import com.sun.guardian.ip.filter.core.service.statistics.IpFilterStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * IP 黑白名单过滤器
 * <p>匹配优先级：全局黑名单 > URL 绑定白名单 > 放行</p>
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-24 20:29
 */
public class IpFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IpFilter.class);
    private static final GuardianLogUtils logUtils = new GuardianLogUtils("[Guardian-Ip-Filter]", "");

    private final IpFilterConfig ipFilterConfig;
    private final ResponseUtils responseUtils;
    private final IpFilterStatistics statistics;

    /**
     * 构造 IP 黑白名单过滤器
     */
    public IpFilter(IpFilterConfig ipFilterConfig, IpFilterResponseHandler responseHandler,
                    IpFilterStatistics statistics, GuardianMessageResolver messageResolver) {
        this.ipFilterConfig = ipFilterConfig;
        this.statistics = statistics;
        this.responseUtils = new ResponseUtils(ipFilterConfig, responseHandler, IpForbiddenException::new, messageResolver);
    }

    /**
     * 执行 IP 黑白名单过滤逻辑
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = ServletUtil.getClientIP(request);
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathWithoutContext = MatchUrlRuleUtils.stripContextPath(requestUri, contextPath);

        if (matchesAny(clientIp, ipFilterConfig.getBlackList())) {
            statistics.recordBlackListBlock(clientIp);
            logUtils.ipBlackBlockLog(ipFilterConfig.isLogEnabled(), log, clientIp, requestUri);
            responseUtils.reject403(request, response, ipFilterConfig.getMessage());
            return;
        }

        UrlWhiteRule rule = MatchUrlRuleUtils.matchUrlRule(ipFilterConfig.getUrls(), requestUri, pathWithoutContext);
        if (rule != null) {
            if (matchesAny(clientIp, rule.getWhiteList())) {
                filterChain.doFilter(request, response);
            } else {
                statistics.recordWhiteListBlock(clientIp, requestUri);
                logUtils.ipWhiteBlockLog(ipFilterConfig.isLogEnabled(), log, clientIp, requestUri);
                responseUtils.reject403(request, response, ipFilterConfig.getMessage());
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判断客户端 IP 是否匹配任意规则（精确 / 通配符 / CIDR）
     */
    private boolean matchesAny(String clientIp, List<String> rules) {
        return rules != null && rules.stream().anyMatch(rule -> IpMatcher.matches(clientIp, rule));
    }
}
