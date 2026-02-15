package com.sun.guardian.example.rateLimit.exception;

import com.sun.guardian.core.exception.RateLimitException;
import com.sun.guardian.example.common.CommonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 限流全局异常处理器（response-mode=exception 时生效）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09
 */
@RestControllerAdvice
public class RateLimitExceptionHandler {

    /** 处理限流异常 */
    @ExceptionHandler(RateLimitException.class)
    public CommonResult<?> handleRateLimitException(RateLimitException e) {
        return CommonResult.error(e.getMessage());
    }
}
