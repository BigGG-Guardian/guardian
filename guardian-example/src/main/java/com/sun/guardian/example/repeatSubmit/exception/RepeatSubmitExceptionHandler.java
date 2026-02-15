package com.sun.guardian.example.repeatSubmit.exception;

import com.sun.guardian.core.exception.RepeatSubmitException;
import com.sun.guardian.example.common.CommonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 防重全局异常处理器（response-mode=exception 时生效）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 15:33
 */
@RestControllerAdvice
public class RepeatSubmitExceptionHandler {

    /** 处理重复提交异常 */
    @ExceptionHandler(RepeatSubmitException.class)
    public CommonResult<?> handleRepeatSubmitException(RepeatSubmitException e) {
        return CommonResult.error(e.getMessage());
    }
}
