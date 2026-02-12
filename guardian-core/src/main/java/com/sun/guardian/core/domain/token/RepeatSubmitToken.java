package com.sun.guardian.core.domain.token;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

/**
 * 防重令牌
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 19:47
 */
@Accessors(chain = true)
@Data
public class RepeatSubmitToken {

    /**
     * 防重键
     */
    private String key;

    /**
     * 过期时间
     */
    private long timeout;

    /**
     * 时间单位
     */
    private TimeUnit timeoutUnit;

    /**
     * 令牌创建时间
     */
    private long createTime;

}
