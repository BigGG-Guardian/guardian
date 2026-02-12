package com.sun.guardian.repeat.submit.core.enums.scope;

/**
 * 防重键维度枚举
 * <p>
 * 控制防重 Key 的粒度，粒度越粗拦截范围越大：
 * <ul>
 *   <li>{@link #USER} — 每个用户独立判断，互不影响（默认）</li>
 *   <li>{@link #IP} — 同一 IP 下所有用户共享防重，适合防刷场景</li>
 *   <li>{@link #GLOBAL} — 全局共享，适合抢购/唯一资源创建场景</li>
 * </ul>
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12 22:53
 * @see com.sun.guardian.repeat.submit.core.constants.KeyPrefixConstants
 */
public enum KeyScope {

    /**
     * 用户级（默认）：同一用户 + 同一接口 + 同一参数视为重复
     */
    USER("user"),

    /**
     * IP 级：同一 IP + 同一接口 + 同一参数视为重复，不区分用户
     */
    IP("ip"),

    /**
     * 全局级：同一接口 + 同一参数视为重复，不区分用户和 IP
     */
    GLOBAL("global");

    /**
     * yml 配置中使用的标识值
     */
    public final String key;

    KeyScope(String key) {
        this.key = key;
    }
}
