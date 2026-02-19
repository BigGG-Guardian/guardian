package com.sun.guardian.core.utils;

import com.sun.guardian.core.enums.response.ResponseMode;
import com.sun.guardian.core.service.response.GuardianResponseHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Function;

/**
 * 响应处理工具类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 17:29
 */
public class ResponseUtils {

    private final ResponseMode responseMode;
    private final GuardianResponseHandler responseHandler;
    private final Function<String, RuntimeException> exceptionFactory;

    public ResponseUtils(ResponseMode responseMode,
                         GuardianResponseHandler responseHandler,
                         Function<String, RuntimeException> exceptionFactory) {
        this.responseMode = responseMode;
        this.responseHandler = responseHandler;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * 直接写入原始 JSON 字符串（用于缓存命中等场景，不做二次包装）
     */
    public void writeRawJson(HttpServletResponse response, String json) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }

    /**
     * 拒绝请求：JSON 模式直接写响应，EXCEPTION 模式抛出异常
     *
     * @return false（JSON 模式下中断拦截器链）
     */
    public boolean reject(HttpServletRequest request, HttpServletResponse response,
                          String message) throws IOException {
        if (responseMode == ResponseMode.JSON) {
            responseHandler.handle(request, response, 500, null, message);
            return false;
        }
        throw exceptionFactory.apply(message);
    }
}
