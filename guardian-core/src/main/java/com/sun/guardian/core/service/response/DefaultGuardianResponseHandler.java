package com.sun.guardian.core.service.response;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 默认 JSON 响应处理器
 * <p>
 * 返回格式：{@code {"code":500,"msg":"提示信息","timestamp":1234567890}}
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09
 */
public class DefaultGuardianResponseHandler implements GuardianResponseHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        JSONObject result = new JSONObject();
        result.set("code", 500);
        result.set("msg", message);
        result.set("timestamp", DateUtil.current());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }
}
