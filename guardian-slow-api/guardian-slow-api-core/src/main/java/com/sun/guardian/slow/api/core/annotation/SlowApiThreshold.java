package com.sun.guardian.slow.api.core.annotation;

import java.lang.annotation.*;

/**
 * 慢接口检测注解
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 18:22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SlowApiThreshold {

    /**
     * 慢接口阈值（毫秒）
     */
    long value() default 3000;
}
