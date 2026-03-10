package com.sun.guardian.encrypt.core.enums.mode;

/**
 * 参数加密密钥模式
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 19:05
 */
public enum DataKeyMode {
    /**
     * 静态密钥，根据配置的key进行加密解密参数
     */
    STATIC,
    /**
     * 动态密钥，每次请求动态生成新的密钥进行加密解密参数
     */
    DYNAMIC;
}
