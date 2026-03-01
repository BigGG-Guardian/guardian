package com.sun.guardian.api.switchs.core.interceptor;

import com.sun.guardian.api.switchs.core.config.ApiSwitchConfig;
import com.sun.guardian.api.switchs.core.service.manager.ApiSwitchManager;
import com.sun.guardian.api.switchs.core.service.response.ApiSwitchResponseHandler;
import com.sun.guardian.core.exception.ApiDisabledException;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.core.utils.ip.IpUtils;
import com.sun.guardian.core.utils.log.GuardianLogUtils;
import com.sun.guardian.core.utils.match.MatchUrlRuleUtils;
import com.sun.guardian.core.utils.response.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 接口开关核心拦截器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-28 21:53
 */
public class ApiSwitchInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiSwitchInterceptor.class);
    private static final GuardianLogUtils logUtils = new GuardianLogUtils("[Guardian-Api-Switch]", "");

    private final ApiSwitchConfig switchConfig;
    private final ApiSwitchManager switchManager;
    private final ResponseUtils responseUtils;

    public ApiSwitchInterceptor(ApiSwitchConfig switchConfig, ApiSwitchResponseHandler responseHandler, GuardianMessageResolver messageResolver) {
        this.switchConfig = switchConfig;
        this.switchManager = new ApiSwitchManager(switchConfig);
        this.responseUtils = new ResponseUtils(switchConfig, responseHandler, ApiDisabledException::new, messageResolver);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathWithoutContext = MatchUrlRuleUtils.stripContextPath(requestUri, contextPath);
        String ip = IpUtils.getClientIp(request);

        if (switchManager.isDisabled(requestUri, pathWithoutContext)) {
            logUtils.disabledLog(switchConfig.isLogEnabled(), log, requestUri, ip);
            responseUtils.reject(request, response, switchConfig.getMessage());
            return false;
        }

        logUtils.passLog(switchConfig.isLogEnabled(), log, requestUri, ip);
        return true;
    }
}
