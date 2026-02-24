package com.sun.guardian.ip.filter.core.config;

import com.sun.guardian.core.service.base.BaseConfig;
import com.sun.guardian.ip.filter.core.domain.rule.UrlWhiteRule;

import java.util.List;

/**
 * IP 黑白名单配置属性接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-24 20:30
 */
public interface IpFilterConfig extends BaseConfig {

    /**
     * URL 绑定白名单规则列表 Getter
     */
    List<UrlWhiteRule> getUrls();

    /**
     * 全局 IP 黑名单 Getter
     */
    List<String> getBlackList();

    /**
     * 拒绝提示信息（支持 i18n Key）Getter
     */
    default String getMessage() {
        return "IP 访问被拒绝";
    }

}
