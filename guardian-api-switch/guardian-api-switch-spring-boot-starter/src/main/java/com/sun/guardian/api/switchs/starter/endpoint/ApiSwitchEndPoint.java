package com.sun.guardian.api.switchs.starter.endpoint;

import com.sun.guardian.api.switchs.core.config.ApiSwitchConfig;
import com.sun.guardian.api.switchs.core.service.manager.ApiSwitchManager;
import com.sun.guardian.core.utils.url.UrlUtils;
import org.springframework.boot.actuate.endpoint.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 接口开关 Actuator 端点（GET /actuator/guardianApiSwitch）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-28 22:07
 */
@Endpoint(id = "guardianApiSwitch")
public class ApiSwitchEndPoint {

    private final ApiSwitchManager switchManager;

    public ApiSwitchEndPoint(ApiSwitchConfig switchConfig) {
        this.switchManager = new ApiSwitchManager(switchConfig);
    }

    /**
     * 查询已关闭接口
     */
    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("disabledUrls", switchManager.getDisabledUrls());
        return result;
    }

    /**
     * 关闭接口
     */
    @WriteOperation
    public Map<String, Object> disable(@Selector(match = Selector.Match.ALL_REMAINING) String urlPattern) {
        urlPattern = UrlUtils.addSlash(urlPattern);
        switchManager.disable(urlPattern);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("action", "disabled");
        result.put("urlPattern", urlPattern);
        result.put("disabledUrls", switchManager.getDisabledUrls());
        return result;
    }

    /**
     * 开启接口
     */
    @DeleteOperation
    public Map<String, Object> enable(@Selector(match = Selector.Match.ALL_REMAINING) String urlPattern) {
        urlPattern = UrlUtils.addSlash(urlPattern);
        switchManager.enable(urlPattern);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("action", "enabled");
        result.put("urlPattern", urlPattern);
        result.put("disabledUrls", switchManager.getDisabledUrls());
        return result;
    }
}
