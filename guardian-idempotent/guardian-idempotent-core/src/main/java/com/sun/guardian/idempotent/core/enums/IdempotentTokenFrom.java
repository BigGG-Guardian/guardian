package com.sun.guardian.idempotent.core.enums;

/**
 * 接口幂等Token来源
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 10:55
 */
public enum IdempotentTokenFrom {
    /**
     * 从请求头获取
     */
    HEADER,
    /**
     * 从请求参数获取（优先 URL 参数 / 表单字段，其次解析 JSON Body）
     */
    PARAM;


}
