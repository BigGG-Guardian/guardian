package com.sun.guardian.repeat.submit.core.constants;

import java.util.Objects;

import static com.sun.guardian.repeat.submit.core.enums.scope.KeyScope.GLOBAL;
import static com.sun.guardian.repeat.submit.core.enums.scope.KeyScope.IP;

/**
 * 防重 Key 模板常量
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 21:33
 */
public interface KeyPrefixConstants {

    /** 外层前缀模板 */
    String DEFAULT_KEY_PREFIX = "guardian:%s";

    /** 用户级：uri + method + ip + client + userId + args */
    String USER_KEY_SUFFIX = "{servletUri}:{method}:{clientIp}:{client}:{userId}:{args}";

    /** IP 级：uri + method + ip + args */
    String IP_KEY_SUFFIX = "{servletUri}:{method}:{clientIp}:{args}";

    /** 全局级：uri + method + args */
    String GLOBAL_KEY_SUFFIX = "{servletUri}:{method}:{args}";

    /** 按维度获取 Key 模板 */
    static String getSuffixByKeyScope(String keyScope) {
        if (Objects.equals(IP.key, keyScope)) {
            return IP_KEY_SUFFIX;
        } else if (Objects.equals(GLOBAL.key, keyScope)) {
            return GLOBAL_KEY_SUFFIX;
        } else {
            return USER_KEY_SUFFIX;
        }
    }
}
