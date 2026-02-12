package com.sun.guardian.repeat.submit.core.annotation;

import com.sun.guardian.repeat.submit.core.enums.client.ClientType;
import com.sun.guardian.repeat.submit.core.enums.scope.KeyScope;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 重复提交检查注解
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 19:24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    /**
     * 防重时间间隔（默认5）
     */
    int interval() default 5;

    /**
     * 时间单位（默认秒）
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 错误提示信息
     */
    String message() default "您的请求过于频繁，请稍后再试";

    /**
     * 防重键维度
     */
    KeyScope keyScope() default KeyScope.USER;

    /**
     * 客户端类型
     */
    ClientType clientType() default ClientType.PC;
}
