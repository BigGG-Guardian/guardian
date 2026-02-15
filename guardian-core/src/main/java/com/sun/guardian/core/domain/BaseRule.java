package com.sun.guardian.core.domain;

import lombok.Data;

/**
 * 规则基类
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 17:49
 */
@Data
public class BaseRule {
    /**
     * URL 匹配模式（仅 yml 配置时使用，支持 AntPath 通配符）
     */
    private String pattern;
}
