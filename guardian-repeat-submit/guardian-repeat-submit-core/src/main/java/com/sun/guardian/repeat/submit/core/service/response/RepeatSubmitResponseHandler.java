package com.sun.guardian.repeat.submit.core.service.response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 重复提交响应处理器
 * <p>
 * 当 {@code guardian.repeat-submit.response-mode=json} 时，
 * 由该处理器负责向客户端输出 JSON 响应。
 * 用户可自定义实现此接口以适配项目统一返回格式。
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 14:50
 */
@FunctionalInterface
public interface RepeatSubmitResponseHandler {

    /**
     * 处理重复提交的响应输出
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param message  防重提示信息
     */
    void handle(HttpServletRequest request, HttpServletResponse response, String message) throws IOException;
}
