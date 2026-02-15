package com.sun.guardian.rate.limit.core.domain.key;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 限流键数据载体
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 16:44
 */
@Accessors(chain = true)
@Data
public class RateLimitKey {

    private String servletUri;
    private String method;
    private String userId;
    private String clientIp;
    private String keyScope;
}
