package com.sun.guardian.ip.filter.core.service.response;

import com.sun.guardian.core.service.response.GuardianResponseHandler;

/**
 * IP 黑白名单响应处理器（response-mode = json 时使用）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 14:50
 */
@FunctionalInterface
public interface IpFilterResponseHandler extends GuardianResponseHandler {
}
