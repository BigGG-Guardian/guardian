package com.sun.guardian.api.switchs.starter.properties;

import com.sun.guardian.api.switchs.core.config.ApiSwitchConfig;
import com.sun.guardian.core.enums.response.ResponseMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口开关配置参数
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-28 21:29
 */
@Data
@ConfigurationProperties(prefix = "guardian.api-switch")
public class GuardianApiSwitchProperties implements ApiSwitchConfig {

    /**
     * 总开关
     */
    private boolean enabled = true;

    /**
     * 提示信息
     */
    private String message = "接口暂时关闭，请稍后再试";

    /**
     * 拦截器排序（值越小越先执行）
     */
    private int interceptorOrder = -2000;

    /**
     * 响应模式：exception / json
     */
    private ResponseMode responseMode = ResponseMode.EXCEPTION;

    /**
     * 是否打印拦截日志（默认 false）
     */
    private boolean logEnabled = false;

    /**
     * 默认关闭的接口
     */
    private List<String> disabledUrls = new ArrayList<>();
}
