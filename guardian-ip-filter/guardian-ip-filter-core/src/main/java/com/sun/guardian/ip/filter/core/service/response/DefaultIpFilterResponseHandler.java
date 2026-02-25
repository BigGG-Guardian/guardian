package com.sun.guardian.ip.filter.core.service.response;

import com.sun.guardian.core.domain.BaseResult;
import com.sun.guardian.core.service.response.DefaultGuardianResponseHandler;
import com.sun.guardian.core.utils.json.GuardianJsonUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * IP 黑白名单默认 JSON 响应处理器
 * <p>返回格式：{"code":403,"msg":"提示信息","timestamp":1234567890}</p>
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 15:02
 */
public class DefaultIpFilterResponseHandler extends DefaultGuardianResponseHandler
        implements IpFilterResponseHandler {

    /**
     * 写出 403 Forbidden JSON 响应
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Integer code, Object data, String message) throws IOException {
        BaseResult result = BaseResult.result(code, data, message);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(GuardianJsonUtils.toJsonStr(result));
    }
}
