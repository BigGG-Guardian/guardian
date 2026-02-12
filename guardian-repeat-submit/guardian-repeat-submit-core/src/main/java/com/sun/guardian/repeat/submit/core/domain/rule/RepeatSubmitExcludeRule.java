package com.sun.guardian.repeat.submit.core.domain.rule;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 防重提交排除规则（白名单）
 * <p>
 * 通过 yml 配置，命中的 URL 直接跳过所有防重检查（包括 YAML 规则和 {@code @RepeatSubmit} 注解），
 * 优先级最高。
 * <pre>
 * guardian:
 *   exclude-urls:
 *     - pattern: /api/public/**
 * </pre>
 *
 * @author scj
 * @since 2026-02-09
 * @see com.sun.guardian.repeat.submit.core.utils.MatchUrlRuleUtils#matchExcludeUrlRule
 */
@Data
@Accessors(chain = true)
public class RepeatSubmitExcludeRule {

    /**
     * URL 匹配模式，支持 AntPath 通配符（{@code *}、{@code **}、{@code ?}），
     * 自动兼容 context-path（带或不带均可匹配）
     */
    private String pattern;
}
