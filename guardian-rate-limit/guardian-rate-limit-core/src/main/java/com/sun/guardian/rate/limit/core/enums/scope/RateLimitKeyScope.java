package com.sun.guardian.rate.limit.core.enums.scope;

/**
 * 限流维度枚举
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 16:09
 */
public enum RateLimitKeyScope {
    /**
     * 用户级：uri + method + userId
     */
    USER("user"),

    /**
     * IP 级：uri + method + clientIp
     */
    IP("ip"),

    /**
     * 全局级（默认）：uri + method
     */
    GLOBAL("global");

    /**
     * yml 配置中使用的标识值
     */
    public final String key;

    RateLimitKeyScope(String key) {
        this.key = key;
    }
}
