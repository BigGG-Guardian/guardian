package com.sun.guardian.core.enums.response;

/**
 * 响应模式枚举
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 14:42
 */
public enum ResponseMode {

    /**
     * 异常模式（默认），
     * 由业务 {@code @RestControllerAdvice} 全局异常处理器捕获并统一返回
     */
    EXCEPTION,

    /**
     * JSON 模式
     * 直接向客户端写入 JSON 响应，不抛出异常
     */
    JSON
}
