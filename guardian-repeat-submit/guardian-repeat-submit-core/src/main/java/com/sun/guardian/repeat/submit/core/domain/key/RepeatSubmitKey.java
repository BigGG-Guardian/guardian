package com.sun.guardian.repeat.submit.core.domain.key;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 防重键数据载体
 * <p>
 * 承载生成防重 Key 所需的各维度信息，字段名与
 * {@link com.sun.guardian.repeat.submit.core.constants.KeyPrefixConstants} 中模板占位符一一对应。
 * <p>
 * 由 {@link com.sun.guardian.repeat.submit.core.service.key.strategy.AbstractKeyGenerator} 组装，
 * 传递给子类的 {@code buildKey()} 方法进行模板填充。
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 20:25
 */
@Accessors(chain = true)
@Data
public class RepeatSubmitKey {

    /**
     * 用户标识（已登录→userId / 未登录→sessionId / 无session→IP）
     */
    private String userId;

    /**
     * 防重维度标识，对应 {@link com.sun.guardian.repeat.submit.core.enums.scope.KeyScope#key}
     * <p>
     * 决定最终 Key 使用哪套模板（user / ip / global）
     */
    private String keyScope;

    /**
     * 客户端类型标识，对应 {@link com.sun.guardian.repeat.submit.core.enums.client.ClientType#key}
     */
    private String client;

    /**
     * 客户端 IP 地址
     */
    private String clientIp;

    /**
     * HTTP 请求方法（GET / POST / PUT / DELETE 等）
     */
    private String method;

    /**
     * 请求路径（不含 context-path）
     */
    private String servletUri;

    /**
     * 请求参数（排序后的 JSON，Base64 编码）
     */
    private String args;
}
