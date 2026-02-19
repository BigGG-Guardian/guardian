package com.sun.guardian.idempotent.core.annotation;

import com.sun.guardian.idempotent.core.enums.IdempotentTokenFrom;

import java.lang.annotation.*;

/**
 * 接口幂等注解
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 10:53
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 接口唯一标识，用于隔离不同接口的Token
     */
    String value();

    /**
     * Token来源，默认从请求头获取
     */
    IdempotentTokenFrom from() default IdempotentTokenFrom.HEADER;

    /**
     * Token 参数名（Header 名 / URL 参数名 / JSON Body 字段名）
     */
    String tokenName() default "X-Idempotent-Token";

    /**
     * 拦截提示信息
     */
    String message() default "幂等Token无效或已消费";
}
