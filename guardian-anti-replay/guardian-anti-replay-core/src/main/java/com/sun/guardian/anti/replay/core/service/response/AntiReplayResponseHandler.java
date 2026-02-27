package com.sun.guardian.anti.replay.core.service.response;

import com.sun.guardian.core.service.response.GuardianResponseHandler;

/**
 * 防重放攻击 JSON 响应处理器（response-mode=json 时使用）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 14:50
 */
@FunctionalInterface
public interface AntiReplayResponseHandler extends GuardianResponseHandler {
}
