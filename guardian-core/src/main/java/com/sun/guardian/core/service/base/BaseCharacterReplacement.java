package com.sun.guardian.core.service.base;

/**
 * 请求自动trim模块字符替换规则接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-22 11:17
 */
public interface BaseCharacterReplacement {

    /**
     * 待替换字符的转义表示，支持 \\r \\n \\t \\0 \\\\ \\uXXXX Getter
     */
    String getFrom();

    /**
     * 替换为的目标字符串，默认空字符串（即删除）Getter
     */
    String getTo();
}
