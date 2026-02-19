package com.sun.guardian.idempotent.starter.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 接口幂等获取token返回
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-19 15:48
 */
@Accessors(chain = true)
@Data
public class IdempotentTokenControllerResp {
    /**
     * token 信息
     */
    private String token;

    /**
     * 有效时间
     */
    private long expireIn;

    /**
     * 有效时间单位
     */
    private String expireUnit;
}
