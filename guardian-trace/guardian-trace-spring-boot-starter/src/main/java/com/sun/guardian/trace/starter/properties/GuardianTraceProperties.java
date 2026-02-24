package com.sun.guardian.trace.starter.properties;

import com.sun.guardian.trace.core.config.TraceConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 请求链路 TraceId 配置参数
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 19:41
 */
@Data
@ConfigurationProperties(prefix = "guardian.trace")
public class GuardianTraceProperties implements TraceConfig {
    /**
     * 总开关
     */
    private boolean enabled = true;
    /**
     * Filter 排序（值越小越先执行，默认 -30000，确保最先执行以覆盖全链路）
     */
    private int filterOrder = -30000;
    /**
     * TraceId 请求头/响应头名称
     */
    private String headerName = "X-Trace-Id";
}
