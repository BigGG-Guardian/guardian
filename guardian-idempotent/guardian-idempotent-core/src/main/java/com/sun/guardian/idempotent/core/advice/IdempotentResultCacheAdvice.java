package com.sun.guardian.idempotent.core.advice;

import com.sun.guardian.core.utils.json.GuardianJsonUtils;
import com.sun.guardian.idempotent.core.config.IdempotentConfig;
import com.sun.guardian.idempotent.core.domain.result.IdempotentResult;
import com.sun.guardian.idempotent.core.storage.IdempotentResultCache;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 接口幂等返回值存储
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 20:11
 */
@ControllerAdvice
public class IdempotentResultCacheAdvice implements ResponseBodyAdvice<Object>, Ordered {
    private final IdempotentResultCache resultCache;
    private final int order;

    public static final String ATTR_KEY = "guardian_idempotent_result";

    /**
     * 构造幂等返回值缓存切面
     */
    public IdempotentResultCacheAdvice(IdempotentResultCache resultCache, IdempotentConfig config) {
        this.resultCache = resultCache;
        this.order = config.getResultAdviceOrder();
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * 判断是否支持返回值缓存
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 写入响应体前缓存幂等返回值
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (!(request instanceof ServletServerHttpRequest)) {
            return body;
        }
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String resultCacheStr = (String) servletRequest.getAttribute(ATTR_KEY);

        if (StringUtils.hasText(resultCacheStr)) {
            IdempotentResult result = GuardianJsonUtils.toBean(resultCacheStr, IdempotentResult.class);
            result.setJsonResult(body == null ? "null" : GuardianJsonUtils.toJsonStr(body));
            resultCache.cacheResult(result);
        }

        return body;
    }
}
