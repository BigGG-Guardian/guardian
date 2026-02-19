package com.sun.guardian.idempotent.core.domain.result;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

/**
 * 接口幂等返回值
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 19:55
 */
@Accessors(chain = true)
@Data
public class IdempotentResult {
    /**
     * 完整Key（含前缀+接口标识+token）
     */
    private String key;

    /**
     * 返回值Json字符串
     */
    private String jsonResult;

    /**
     * 有效期
     */
    private long timeout;

    /**
     * 有效期单位
     */
    private TimeUnit timeUnit;
}
