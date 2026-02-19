package com.sun.guardian.example.rateLimit.config;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.rate.limit.core.service.response.RateLimitResponseHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletResponse;

/**
 * 限流模块自定义 Bean 配置
 * <p>
 * 自定义 {@link RateLimitResponseHandler}，返回 {@link CommonResult} 格式。
 * {@code UserContext} 已在 RepeatSubmitBeanConfig 中注册，无需重复定义。
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09
 */
@Configuration
public class RateLimitBeanConfig {

    /**
     * 自定义限流 JSON 响应处理器（response-mode=json 时生效）
     */
    @Bean
    public RateLimitResponseHandler rateLimitResponseHandler() {
        return (request, response, code, data, message) -> {
            JSONConfig jsonConfig = new JSONConfig();
            jsonConfig.setIgnoreNullValue(false);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(CommonResult.result(code, data, message), jsonConfig));
        };
    }
}
