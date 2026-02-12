package com.sun.guardian.core.enums.client;

/**
 * 客户端类型
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 21:28
 */
public enum ClientType {
    /**
     * PC端
     */
    PC("PC"),
    /**
     * 移动端
     */
    MOBILE("Mobile"),
    ;

    public final String key;

    ClientType(String key) {
        this.key = key;
    }
}
