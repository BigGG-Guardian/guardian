package com.sun.guardian.api.switchs.core.service.manager;

import com.sun.guardian.api.switchs.core.config.ApiSwitchConfig;
import com.sun.guardian.core.utils.match.MatchUrlRuleUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口开关管理器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-28 21:39
 */
public class ApiSwitchManager {
    private static final Set<String> actuatorDisabledUrls = ConcurrentHashMap.newKeySet();
    private static final Set<String> actuatorEnabledUrls = ConcurrentHashMap.newKeySet();
    private final ApiSwitchConfig switchConfig;

    public ApiSwitchManager(ApiSwitchConfig switchConfig) {
        this.switchConfig = switchConfig;
    }

    /**
     * 判断接口是否关闭
     */
    public boolean isDisabled(String requestUri, String pathWithoutContext) {
        return MatchUrlRuleUtils.matchUrl(getDisabledUrls(), requestUri, pathWithoutContext) != null;
    }

    /**
     * actuator关闭接口
     */
    public void disable(String urlPattern) {
        actuatorDisabledUrls.add(urlPattern);
        actuatorEnabledUrls.remove(urlPattern);
    }

    /**
     * actuator开启接口
     */
    public void enable(String urlPattern) {
        actuatorEnabledUrls.add(urlPattern);
        actuatorDisabledUrls.remove(urlPattern);
    }


    /**
     * 获取关闭接口
     */
    public Set<String> getDisabledUrls() {
        List<String> ymlDisabled = switchConfig.getDisabledUrls(); // 每次获取最新 yml 配置
        Set<String> result = ConcurrentHashMap.newKeySet();

        if (ymlDisabled != null) {
            result.addAll(ymlDisabled);
            result.removeAll(actuatorEnabledUrls);
        }

        result.addAll(actuatorDisabledUrls);

        return Collections.unmodifiableSet(result);
    }
}
