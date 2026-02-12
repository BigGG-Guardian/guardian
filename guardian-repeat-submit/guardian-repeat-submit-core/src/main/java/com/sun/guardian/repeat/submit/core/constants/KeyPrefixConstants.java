package com.sun.guardian.repeat.submit.core.constants;

import cn.hutool.core.util.StrUtil;

import static com.sun.guardian.repeat.submit.core.enums.scope.KeyScope.GLOBAL;
import static com.sun.guardian.repeat.submit.core.enums.scope.KeyScope.IP;

/**
 * 防重键模板常量
 * <p>
 * 定义不同维度（用户级 / IP 级 / 全局级）的 Key 模板，
 * 占位符名称与 {@link com.sun.guardian.repeat.submit.core.domain.key.RepeatSubmitKey} 字段名一一对应，
 * 由 {@link com.sun.guardian.repeat.submit.core.utils.TemplateUtil#formatByBean} 按名称填充。
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 21:33
 */
public interface KeyPrefixConstants {

    /**
     * 防重键外层前缀模板，{} 由加密后的完整 Key 替换
     * <p>
     * 最终存储的 Key 示例：{@code guardian:a1b2c3d4e5}
     */
    String DEFAULT_KEY_PREFIX = "guardian:{}";

    /**
     * 用户级模板 — 同一用户 + 同一接口 + 同一参数视为重复
     * <p>
     * 维度：请求路径 + 请求方法 + IP + 客户端类型 + 用户ID + 参数
     */
    String USER_KEY_SUFFIX = "{servletUri}:{method}:{clientIp}:{client}:{userId}:{args}";

    /**
     * IP 级模板 — 同一 IP + 同一接口 + 同一参数视为重复（不区分用户）
     * <p>
     * 维度：请求路径 + 请求方法 + IP + 参数
     */
    String IP_KEY_SUFFIX = "{servletUri}:{method}:{clientIp}:{args}";

    /**
     * 全局级模板 — 同一接口 + 同一参数视为重复（不区分用户和 IP）
     * <p>
     * 维度：请求路径 + 请求方法 + 参数
     */
    String GLOBAL_KEY_SUFFIX = "{servletUri}:{method}:{args}";

    /**
     * 根据防重维度获取对应的 Key 模板
     *
     * @param keyScope 维度标识，对应 {@link com.sun.guardian.repeat.submit.core.enums.scope.KeyScope#key}
     * @return 对应维度的 Key 模板字符串
     */
    static String getSuffixByKeyScope(String keyScope) {
        if (StrUtil.equals(IP.key, keyScope)) {
            return IP_KEY_SUFFIX;
        } else if (StrUtil.equals(GLOBAL.key, keyScope)) {
            return GLOBAL_KEY_SUFFIX;
        } else {
            return USER_KEY_SUFFIX;
        }
    }
}
