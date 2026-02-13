package com.sun.guardian.repeat.submit.core.enums.response;

/**
 * 重复提交响应模式
 * <ul>
 *   <li>{@link #EXCEPTION} — 抛出异常，由业务全局异常处理器统一返回（默认）</li>
 *   <li>{@link #JSON} — 拦截器直接写入 JSON 响应，不抛出异常</li>
 * </ul>
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 14:42
 * @see com.sun.guardian.repeat.submit.core.service.response.RepeatSubmitResponseHandler
 */
public enum ResponseMode {

    /**
     * 异常模式（默认）：抛出 {@code RepeatSubmitException}，
     * 由业务 {@code @RestControllerAdvice} 全局异常处理器捕获并统一返回
     */
    EXCEPTION,

    /**
     * JSON 模式：由 {@link com.sun.guardian.repeat.submit.core.service.response.RepeatSubmitResponseHandler}
     * 直接向客户端写入 JSON 响应，不抛出异常
     */
    JSON
}
