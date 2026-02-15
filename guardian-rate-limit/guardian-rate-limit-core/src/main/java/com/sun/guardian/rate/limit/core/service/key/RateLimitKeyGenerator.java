package com.sun.guardian.rate.limit.core.service.key;

import com.sun.guardian.rate.limit.core.domain.rule.RateLimitRule;
import com.sun.guardian.rate.limit.core.domain.token.RateLimitToken;

import javax.servlet.http.HttpServletRequest;

/**
 * 接口限流键生成接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 16:59
 */
public interface RateLimitKeyGenerator {

    /** 生成限流令牌 */
    RateLimitToken generate(RateLimitRule rule, HttpServletRequest request);
}
