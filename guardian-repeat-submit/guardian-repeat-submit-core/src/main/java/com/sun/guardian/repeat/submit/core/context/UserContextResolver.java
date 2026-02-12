package com.sun.guardian.repeat.submit.core.context;

/**
 * 用户上下文解析器（可选实现）
 * <p>
 * 用于获取当前登录用户 ID，作为防重 Key 的用户维度标识。
 * <ul>
 *   <li>已实现：返回当前用户 ID，防重 Key 按用户隔离</li>
 *   <li>未实现：框架自动注册默认实现（返回 null），降级为 SessionId / IP 标识</li>
 * </ul>
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 21:41
 */
public interface UserContextResolver {

    /**
     * 获取当前登录用户 ID
     *
     * @return 用户 ID，未登录时返回 null 即可（框架自动降级为 SessionId / IP）
     */
    String getUserId();
}
