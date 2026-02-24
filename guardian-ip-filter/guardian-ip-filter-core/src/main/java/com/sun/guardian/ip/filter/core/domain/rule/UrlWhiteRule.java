package com.sun.guardian.ip.filter.core.domain.rule;

import com.sun.guardian.core.domain.BaseRule;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * URL 绑定白名单规则，配置该 URL 允许访问的 IP 列表
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-24 20:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UrlWhiteRule extends BaseRule {

    /**
     * 允许访问的 IP 列表（支持精确 / 通配符 / CIDR）
     */
    private List<String> whiteList = new ArrayList<>();
}
