package com.sun.guardian.core.context;

/**
 * 用户上下文解析器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 21:41
 */
public interface UserContextResolver {

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    String getUserId();
}
