package com.sun.guardian.encrypt.core.service.response;

import com.sun.guardian.core.service.response.GuardianResponseHandler;

/**
 * 请求加密解密 JSON 响应处理器（response-mode=json 时使用）
 *
 * @author scj
 * @version java version 1.8
 */
@FunctionalInterface
public interface EncryptResponseHandler extends GuardianResponseHandler {
}
