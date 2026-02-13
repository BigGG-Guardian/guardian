package com.sun.guardian.example.repeatSubmit.exception;

import com.sun.guardian.core.exception.RepeatSubmitException;
import com.sun.guardian.example.common.CommonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 防重复提交全局异常处理器（response-mode=exception 时生效）
 * <p>
 * 捕获 {@link RepeatSubmitException}，返回项目统一格式 {@link CommonResult}。
 * <p>
 * 当 {@code response-mode=json} 时，拦截器直接写入响应，不会走到此处理器。
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 15:33
 */
@RestControllerAdvice
public class RepeatSubmitExceptionHandler {

    /**
     * 处理重复提交异常
     *
     * @param e 重复提交异常
     * @return 统一错误响应
     */
    @ExceptionHandler(RepeatSubmitException.class)
    public CommonResult<?> handleRepeatSubmitException(RepeatSubmitException e) {
        return CommonResult.error(e.getMessage());
    }
}
