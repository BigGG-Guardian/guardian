package com.sun.guardian.sign.core.annotation;

import com.sun.guardian.sign.core.enums.algorithm.SignAlgorithm;

import java.lang.annotation.*;

/**
 * 参数签名检查注解
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-02 21:01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SignVerify {

    /**
     * 签名算法：base64 /md5 / sha256 / hmac-sha256
     */
    SignAlgorithm algorithm() default SignAlgorithm.BASE64;

    /**
     * 签名校验失败提示信息（支持 i18n Key）
     */
    String signVerifyMessage() default "签名校验失败";
}
