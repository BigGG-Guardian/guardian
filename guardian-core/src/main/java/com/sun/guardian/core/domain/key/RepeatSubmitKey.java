package com.sun.guardian.core.domain.key;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 防重键数据载体
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 20:25
 */
@Accessors(chain = true)
@Data
public class RepeatSubmitKey {

    /** 用户ID */
    private String userId;

    /** 客户端类型 */
    private String client;

    /** 客户端IP */
    private String clientIp;

    /** 请求方法（GET/POST等） */
    private String method;

    /** 请求路径 */
    private String servletUri;

    /** 请求参数（排序后的JSON） */
    private String args;
}
