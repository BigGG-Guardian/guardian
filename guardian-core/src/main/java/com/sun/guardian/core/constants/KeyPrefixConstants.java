package com.sun.guardian.core.constants;

/**
 * 防重键常量
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 21:33
 */
public interface KeyPrefixConstants {

    /** 防重键前缀模板 guardian:{加密后的key} */
    String DEFAULT_KEY_PREFIX = "guardian:{}";

    /** 防重键内容模板 方法地址:请求方法:IP:客户端:用户ID:参数 */
    String DEFAULT_KEY_SUFFIX = "{}:{}:{}:{}:{}:{}";
}
