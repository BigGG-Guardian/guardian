package com.sun.guardian.core.utils.response;

import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.core.service.base.BaseConfig;
import com.sun.guardian.core.enums.response.ResponseMode;
import com.sun.guardian.core.service.response.GuardianResponseHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Function;

/**
 * 响应处理工具类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 17:29
 */
public class ResponseUtils {

    private final BaseConfig baseConfig;
    private final GuardianResponseHandler responseHandler;
    private final Function<String, RuntimeException> exceptionFactory;
    private final GuardianMessageResolver messageResolver;

    /**
     * 构造响应处理工具
     */
    public ResponseUtils(BaseConfig baseConfig,
                         GuardianResponseHandler responseHandler,
                         Function<String, RuntimeException> exceptionFactory,
                         GuardianMessageResolver messageResolver) {
        this.baseConfig = baseConfig;
        this.responseHandler = responseHandler;
        this.exceptionFactory = exceptionFactory;
        this.messageResolver = messageResolver;
    }

    /**
     * 直接写入原始 JSON 字符串（用于缓存命中等场景，不做二次包装）
     */
    public void writeRawJson(HttpServletResponse response, String json) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }

    /**
     * 拒绝请求：JSON 模式直接写响应，EXCEPTION 模式抛出异常
     *
     * @return false（JSON 模式下中断拦截器链）
     */
    public boolean reject(HttpServletRequest request, HttpServletResponse response,
                          String message) throws IOException {
        String resolvedMessage = messageResolver.resolve(message);
        if (baseConfig.getResponseMode() == ResponseMode.JSON) {
            responseHandler.handle(request, response, 500, null, resolvedMessage);
            return false;
        }
        throw exceptionFactory.apply(resolvedMessage);
    }

    /**
     * 拒绝请求403：JSON 模式直接写响应，EXCEPTION 模式抛出异常
     *
     * @return false（JSON 模式下中断拦截器链）
     */
    public boolean reject403(HttpServletRequest request, HttpServletResponse response,
                             String message) throws IOException {
        String resolvedMessage = messageResolver.resolve(message);
        if (baseConfig.getResponseMode() == ResponseMode.JSON) {
            responseHandler.handle(request, response, 403, null, resolvedMessage);
            return false;
        }
        throw exceptionFactory.apply(resolvedMessage);
    }
}
