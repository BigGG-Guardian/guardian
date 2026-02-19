package com.sun.guardian.example.idempotent.exception;

import com.sun.guardian.core.exception.IdempotentException;
import com.sun.guardian.example.common.CommonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 接口幂等全局异常处理器（response-mode=exception 时生效）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09
 */
@RestControllerAdvice
public class IdempotentExceptionHandler {

    /** 处理接口幂等异常 */
    @ExceptionHandler(IdempotentException.class)
    public CommonResult<?> handleIdempotentException(IdempotentException e) {
        return CommonResult.error(e.getMessage());
    }
}
