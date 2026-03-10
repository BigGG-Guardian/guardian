package com.sun.guardian.encrypt.core.service.response;

import com.sun.guardian.core.service.response.DefaultGuardianResponseHandler;

/**
 * 请求加密解密 JSON 响应处理器
 * 返回格式：{"code":500,"msg":"提示信息","timestamp":1234567890}
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 15:02
 */
public class DefaultEncryptResponseHandler extends DefaultGuardianResponseHandler
        implements EncryptResponseHandler {
}
