package com.sun.guardian.anti.replay.core.filter;

import com.sun.guardian.anti.replay.core.config.AntiReplayConfig;
import com.sun.guardian.anti.replay.core.domain.rule.AntiReplayRule;
import com.sun.guardian.anti.replay.core.service.response.AntiReplayResponseHandler;
import com.sun.guardian.anti.replay.core.statistics.AntiReplayStatistics;
import com.sun.guardian.anti.replay.core.storage.NonceStorage;
import com.sun.guardian.core.exception.AntiReplayAttackException;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.core.utils.ip.IpUtils;
import com.sun.guardian.core.utils.log.GuardianLogUtils;
import com.sun.guardian.core.utils.match.MatchUrlRuleUtils;
import com.sun.guardian.core.utils.response.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 防重放攻击核心过滤器
 * <p>
 * 校验流程：
 * <ol>
 *   <li>排除规则匹配（exclude-urls）→ 命中直接放行</li>
 *   <li>URL 规则匹配（urls）→ 未命中则放行</li>
 *   <li>Timestamp 校验 → 与服务器时间差超过 maxAge 则拒绝（请求过期）</li>
 *   <li>Nonce 校验 → 已存在于 Storage 则拒绝（重放攻击）</li>
 *   <li>校验通过 → 将 Nonce 存入 Storage（TTL = nonceTtl），放行请求</li>
 * </ol>
 *
 * @author scj
 * @since 2026-02-27
 */
public class AntiReplayFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(AntiReplayFilter.class);
    private static final GuardianLogUtils logUtils = new GuardianLogUtils("[Guardian-Anti-Replay]", "");

    private final AntiReplayConfig replayConfig;
    private final NonceStorage storage;
    private final AntiReplayStatistics statistics;
    private final ResponseUtils responseUtils;

    public AntiReplayFilter(AntiReplayConfig replayConfig, NonceStorage storage, AntiReplayResponseHandler responseHandler, AntiReplayStatistics statistics, GuardianMessageResolver messageResolver) {
        this.replayConfig = replayConfig;
        this.storage = storage;
        this.statistics = statistics;
        this.responseUtils = new ResponseUtils(replayConfig, responseHandler, AntiReplayAttackException::new, messageResolver);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathWithoutContext = MatchUrlRuleUtils.stripContextPath(requestUri, contextPath);
        String ip = IpUtils.getClientIp(request);

        if (MatchUrlRuleUtils.matchExcludeUrlRule(replayConfig.getExcludeUrls(), requestUri, pathWithoutContext)) {
            logUtils.excludeLog(replayConfig.isLogEnabled(), log, requestUri, ip);
            filterChain.doFilter(request, response);
            return;
        }

        AntiReplayRule rule = MatchUrlRuleUtils.matchUrlRule(replayConfig.getUrls(), requestUri, pathWithoutContext);
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String timestampStr = request.getHeader(replayConfig.getTimestampHeader());
        if (!StringUtils.hasText(timestampStr)) {
            statistics.recordBlock(requestUri);
            logUtils.blockLog(replayConfig.isLogEnabled(), log, requestUri, ip);
            responseUtils.reject(request, response, replayConfig.getMissingTimestampMessage());
            return;
        }

        long timestamp = Long.parseLong(timestampStr);
        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > replayConfig.getMaxAgeUnit().toMillis(replayConfig.getMaxAge())) {
            statistics.recordBlock(requestUri);
            logUtils.blockLog(replayConfig.isLogEnabled(), log, requestUri, ip);
            responseUtils.reject(request, response, replayConfig.getExpiredMessage());
            return;
        }

        String nonce = request.getHeader(replayConfig.getNonceHeader());
        if (!StringUtils.hasText(nonce)) {
            statistics.recordBlock(requestUri);
            logUtils.blockLog(replayConfig.isLogEnabled(), log, requestUri, ip);
            responseUtils.reject(request, response, replayConfig.getMissingNonceMessage());
            return;
        }
        if (!storage.tryAcquire(nonce, replayConfig.getNonceTtl(), replayConfig.getNonceTtlUnit())) {
            statistics.recordBlock(requestUri);
            logUtils.blockLog(replayConfig.isLogEnabled(), log, requestUri, ip);
            responseUtils.reject(request, response, replayConfig.getReplayMessage());
            return;
        }

        statistics.recordPass();
        logUtils.passLog(replayConfig.isLogEnabled(), log, requestUri, ip);
        filterChain.doFilter(request, response);
    }
}
