package com.sun.guardian.core.context;

/**
 * 用户上下文接口，获取当前登录用户 ID
 * 实现此接口并注册为 Bean，所有模块共享。
 * 未注册时框架默认返回 null，降级为 SessionId / IP。
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 17:07
 */
@FunctionalInterface
public interface UserContext {

    /**
     * 获取当前登录用户 ID
     *
     * @return 用户 ID，未登录时返回 null 即可（框架自动降级为 SessionId / IP）
     */
    String getUserId();
}
