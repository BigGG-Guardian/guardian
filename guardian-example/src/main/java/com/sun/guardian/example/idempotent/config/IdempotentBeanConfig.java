package com.sun.guardian.example.idempotent.config;

import com.sun.guardian.core.utils.json.GuardianJsonUtils;
import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.idempotent.core.service.response.IdempotentResponseHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletResponse;

/**
 * 接口幂等模块自定义 Bean 配置
 * <p>
 * 自定义 {@link IdempotentResponseHandler}，返回 {@link CommonResult} 格式。
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09
 */
@Configuration
public class IdempotentBeanConfig {

    /**
     * 自定义接口幂等 JSON 响应处理器（response-mode=json 时生效）
     */
    @Bean
    public IdempotentResponseHandler idempotentResponseHandler() {
        return (request, response, code, data, message) -> {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(GuardianJsonUtils.toJsonStr(CommonResult.result(code, data, message)));
        };
    }
}
