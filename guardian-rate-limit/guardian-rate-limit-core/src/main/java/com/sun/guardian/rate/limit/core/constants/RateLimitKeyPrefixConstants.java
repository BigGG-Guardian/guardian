package com.sun.guardian.rate.limit.core.constants;

import cn.hutool.core.util.StrUtil;

import static com.sun.guardian.rate.limit.core.enums.scope.RateLimitKeyScope.*;


/**
 * 限流 Key 模板常量
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 21:33
 */
public interface RateLimitKeyPrefixConstants {

    /** 外层前缀模板 */
    String DEFAULT_KEY_PREFIX = "guardian:rate-limit:{}";

    /** 用户级：uri + method + userId */
    String USER_KEY_SUFFIX = "{servletUri}:{method}:{userId}";

    /** IP 级：uri + method + clientIp */
    String IP_KEY_SUFFIX = "{servletUri}:{method}:{clientIp}";

    /** 全局级：uri + method */
    String GLOBAL_KEY_SUFFIX = "{servletUri}:{method}";

    /** 按维度获取 Key 模板 */
    static String getSuffixByKeyScope(String keyScope) {
        if (StrUtil.equals(IP.key, keyScope)) {
            return IP_KEY_SUFFIX;
        } else if (StrUtil.equals(USER.key, keyScope)) {
            return USER_KEY_SUFFIX;
        } else {
            return GLOBAL_KEY_SUFFIX;
        }
    }
}
