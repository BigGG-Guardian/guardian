package com.sun.guardian.idempotent.core.domain.token;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

/**
 * 接口幂等Token
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 16:47
 */
@Accessors(chain = true)
@Data
public class IdempotentToken {

    /**
     * 完整Key（含前缀+接口标识+token）
     */
    private String key;

    /**
     * 有效期
     */
    private long timeout;

    /**
     * 有效期单位
     */
    private TimeUnit timeUnit;

    /**
     * token创建时间
     */
    private long createTime;
}
