package com.sun.guardian.idempotent.core.domain.result;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 接口幂等返回值信息
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 20:00
 */
@Accessors(chain = true)
@Data
public class IdempotentResultEntity {

    /**
     * 返回值Json字符串
     */
    private String jsonResult;

    /**
     * 过期时间
     */
    private long expireAt;
}
