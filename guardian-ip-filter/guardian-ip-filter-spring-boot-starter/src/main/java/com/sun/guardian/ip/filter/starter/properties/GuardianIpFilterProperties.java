package com.sun.guardian.ip.filter.starter.properties;

import com.sun.guardian.core.enums.response.ResponseMode;
import com.sun.guardian.ip.filter.core.config.IpFilterConfig;
import com.sun.guardian.ip.filter.core.domain.rule.UrlWhiteRule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * IP 黑白名单配置参数
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-24 20:48
 */
@Data
@ConfigurationProperties(prefix = "guardian.ip-filter")
public class GuardianIpFilterProperties implements IpFilterConfig {

    /**
     * 总开关
     */
    private boolean enabled = true;
    /**
     * Filter 排序（值越小越先执行，默认 -20000，确保最先执行以覆盖全链路）
     */
    private int filterOrder = -20000;
    /**
     * 接口白名单
     */
    private List<UrlWhiteRule> urls = new ArrayList<>();
    /**
     * ip黑名单
     */
    private List<String> blackList = new ArrayList<>();
    /**
     * 拒绝提示信息（支持 i18n Key）
     */
    private String message = "IP 访问被拒绝";
    /**
     * 是否打印拦截日志（默认 false）
     */
    private boolean logEnabled = false;
    /**
     * 响应模式：exception / json
     */
    private ResponseMode responseMode = ResponseMode.EXCEPTION;
}
