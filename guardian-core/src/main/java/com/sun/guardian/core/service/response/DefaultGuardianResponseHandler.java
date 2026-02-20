package com.sun.guardian.core.service.response;

import cn.hutool.json.JSONUtil;
import com.sun.guardian.core.domain.BaseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 默认 JSON 响应处理器，返回格式：{"code":500,"msg":"提示信息","timestamp":1234567890}
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09
 */
public class DefaultGuardianResponseHandler implements GuardianResponseHandler {

    /** 将拦截结果以 JSON 格式写入响应 */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Integer code, Object data, String message) throws IOException {
        BaseResult result = BaseResult.result(code, data, message);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }
}
