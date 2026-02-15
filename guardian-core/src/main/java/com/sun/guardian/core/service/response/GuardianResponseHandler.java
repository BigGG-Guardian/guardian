package com.sun.guardian.core.service.response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 响应处理器接口（response-mode=json 时向客户端写入 JSON）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09
 */
@FunctionalInterface
public interface GuardianResponseHandler {

    /** 处理拦截后的响应输出 */
    void handle(HttpServletRequest request, HttpServletResponse response, String message) throws IOException;
}
