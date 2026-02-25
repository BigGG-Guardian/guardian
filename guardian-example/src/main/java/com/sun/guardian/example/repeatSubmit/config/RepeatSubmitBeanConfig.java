package com.sun.guardian.example.repeatSubmit.config;

import com.sun.guardian.core.utils.json.GuardianJsonUtils;
import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.core.context.UserContext;
import com.sun.guardian.repeat.submit.core.service.response.RepeatSubmitResponseHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletResponse;

/**
 * 防重提交模块自定义 Bean 配置
 * <p>
 * 自定义 {@link UserContext} 和 {@link RepeatSubmitResponseHandler}，
 * 返回 {@link CommonResult} 格式。
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 15:24
 */
@Configuration
public class RepeatSubmitBeanConfig {

    /**
     * 自定义用户上下文，未接入登录体系，返回 null 走 sessionId/IP 降级
     */
    @Bean
    public UserContext userContext() {
        return () -> null;
    }

    /**
     * 自定义防重 JSON 响应处理器（response-mode=json 时生效）
     */
    @Bean
    public RepeatSubmitResponseHandler repeatSubmitResponseHandler() {
        return (request, response, code, data, message) -> {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(GuardianJsonUtils.toJsonStr(CommonResult.result(code, data, message)));
        };
    }
}
