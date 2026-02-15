package com.sun.guardian.repeat.submit.core.service.response;

import com.sun.guardian.core.service.response.DefaultGuardianResponseHandler;

/**
 * 防重默认 JSON 响应处理器
 * <p>
 * 返回格式：{@code {"code":500,"msg":"提示信息","timestamp":1234567890}}
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 15:02
 */
public class DefaultRepeatSubmitResponseHandler extends DefaultGuardianResponseHandler
        implements RepeatSubmitResponseHandler {
}
