package com.sun.guardian.example.repeatSubmit.config;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.repeat.submit.core.context.UserContextResolver;
import com.sun.guardian.repeat.submit.core.service.response.RepeatSubmitResponseHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletResponse;

/**
 * Guardian 防重复提交自定义 Bean 配置
 * <p>
 * 演示如何通过注册 Bean 覆盖 Guardian 的默认行为：
 * <ul>
 *   <li>{@link UserContextResolver} — 自定义用户标识获取逻辑（此处返回 null，走 sessionId/IP 降级）</li>
 *   <li>{@link RepeatSubmitResponseHandler} — 自定义 JSON 响应格式，适配项目统一返回体 {@link CommonResult}</li>
 * </ul>
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 15:24
 */
@Configuration
public class RepeatSubmitBeanConfig {

    /**
     * 自定义用户上下文解析器
     * <p>
     * 示例中未接入登录体系，返回 null 走 sessionId/IP 降级。
     * 实际项目中应返回当前登录用户 ID。
     */
    @Bean
    public UserContextResolver userContextResolver() {
        return () -> null;
    }

    /**
     * 自定义 JSON 响应处理器（response-mode=json 时生效）
     * <p>
     * 覆盖 Guardian 默认的简单 JSON 格式，使用项目统一返回体 {@link CommonResult}。
     */
    @Bean
    public RepeatSubmitResponseHandler repeatSubmitResponseHandler() {
        return (request, response, message) -> {
            JSONConfig jsonConfig = new JSONConfig();
            jsonConfig.setIgnoreNullValue(false);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(CommonResult.error(message), jsonConfig));
        };
    }
}
