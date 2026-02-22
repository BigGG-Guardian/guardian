package com.sun.guardian.slow.api.starter.properties;

import com.sun.guardian.slow.api.core.config.SlowApiConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 慢接口检测配置参数
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 19:03
 */
@Data
@ConfigurationProperties(prefix = "guardian.slow-api")
public class GuardianSlowApiProperties implements SlowApiConfig {
    /**
     * 总开关
     */
    private boolean enabled = true;
    /**
     * 拦截器排序（值越小越先执行）
     */
    private int interceptorOrder = -1000;

    /**
     * 慢接口阈值（毫秒，默认 3000）
     */
    private long threshold = 3000;

    /**
     * 排除规则（白名单），优先级最高
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * 校验参数合法性，不合法时抛出 IllegalArgumentException
     */
    public void validate() {
        if (threshold <= 0) {
            throw new IllegalArgumentException("[Guardian-Slow-Api] threshold 必须大于 0，当前值：" + threshold);
        }
    }
}
