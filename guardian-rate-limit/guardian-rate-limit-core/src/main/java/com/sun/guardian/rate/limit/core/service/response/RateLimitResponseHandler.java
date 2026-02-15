package com.sun.guardian.rate.limit.core.service.response;

import com.sun.guardian.core.service.response.GuardianResponseHandler;

/**
 * 限流 JSON 响应处理器（response-mode=json 时使用）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 14:50
 */
@FunctionalInterface
public interface RateLimitResponseHandler extends GuardianResponseHandler {
}
