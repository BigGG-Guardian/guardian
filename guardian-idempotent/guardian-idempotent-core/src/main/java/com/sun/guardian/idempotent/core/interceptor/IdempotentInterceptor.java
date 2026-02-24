package com.sun.guardian.idempotent.core.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sun.guardian.core.exception.IdempotentException;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.core.utils.GuardianLogUtils;
import com.sun.guardian.core.utils.ResponseUtils;
import com.sun.guardian.core.wrapper.RepeatableRequestWrapper;
import com.sun.guardian.idempotent.core.advice.IdempotentResultCacheAdvice;
import com.sun.guardian.idempotent.core.annotation.Idempotent;
import com.sun.guardian.idempotent.core.config.IdempotentConfig;
import com.sun.guardian.idempotent.core.constants.IdempotentKeyPrefixConstants;
import com.sun.guardian.idempotent.core.domain.result.IdempotentResult;
import com.sun.guardian.idempotent.core.enums.IdempotentTokenFrom;
import com.sun.guardian.idempotent.core.service.response.IdempotentResponseHandler;
import com.sun.guardian.idempotent.core.service.statistics.IdempotentStatistics;
import com.sun.guardian.idempotent.core.storage.IdempotentResultCache;
import com.sun.guardian.idempotent.core.storage.IdempotentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 接口幂等拦截器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 17:10
 */
public class IdempotentInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(IdempotentInterceptor.class);
    private static final GuardianLogUtils logUtils = new GuardianLogUtils("[Guardian-Idempotent]", "@Idempotent");
    private final ResponseUtils responseUtils;
    private final IdempotentStorage storage;
    private final IdempotentResultCache resultCache;
    private final IdempotentConfig idempotentConfig;
    private final IdempotentStatistics statistics;


    /**
     * 构造接口幂等拦截器
     */
    public IdempotentInterceptor(IdempotentStorage storage, IdempotentResultCache resultCache, IdempotentResponseHandler idempotentResponseHandler, IdempotentConfig idempotentConfig, IdempotentStatistics statistics, GuardianMessageResolver messageResolver) {
        this.responseUtils = new ResponseUtils(idempotentConfig, idempotentResponseHandler, IdempotentException::new, messageResolver);
        this.storage = storage;
        this.resultCache = resultCache;
        this.idempotentConfig = idempotentConfig;
        this.statistics = statistics;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        Idempotent annotation = ((HandlerMethod) handler).getMethodAnnotation(Idempotent.class);
        if (annotation == null) {
            return true;
        }

        String requestURI = request.getRequestURI();
        String ip = ServletUtil.getClientIP(request);

        String token = (annotation.from() == IdempotentTokenFrom.HEADER)
                ? request.getHeader(annotation.tokenName())
                : resolveParamToken(request, annotation.tokenName());

        if (StrUtil.isBlank(token)) {
            statistics.recordBlock(requestURI);
            logUtils.blockLog(idempotentConfig.isLogEnabled(), log, requestURI, ip);
            return responseUtils.reject(request, response, idempotentConfig.getMissingTokenMessage());
        }

        String fullKey = StrUtil.format(IdempotentKeyPrefixConstants.KEY_PREFIX, annotation.value(), token);
        if (!storage.tryConsume(fullKey)) {
            if (resultCache != null) {
                String cached = resultCache.getCacheResult(fullKey);
                if (cached != null) {
                    logUtils.cacheResultLog(idempotentConfig.isLogEnabled(), log, requestURI, fullKey, ip);
                    statistics.recordPass();
                    if (!"null".equals(cached)) {
                        responseUtils.writeRawJson(response, cached);
                    }
                    return false;
                }
            }
            statistics.recordBlock(requestURI);
            logUtils.blockLog(idempotentConfig.isLogEnabled(), log, requestURI, ip);
            return responseUtils.reject(request, response, annotation.message());
        }

        statistics.recordPass();
        logUtils.passLog(idempotentConfig.isLogEnabled(), log, requestURI, fullKey, ip);

        if (resultCache != null) {
            IdempotentResult result = new IdempotentResult()
                    .setKey(fullKey)
                    .setTimeout(idempotentConfig.getTimeout())
                    .setTimeUnit(idempotentConfig.getTimeUnit());
            request.setAttribute(IdempotentResultCacheAdvice.ATTR_KEY, JSONUtil.toJsonStr(result));
        }

        return true;
    }

    /**
     * PARAM 模式解析 Token：先查 URL 参数 / 表单字段，查不到再从 JSON Body 中按 tokenName 取值
     */
    private String resolveParamToken(HttpServletRequest request, String tokenName) {
        String token = request.getParameter(tokenName);
        if (StrUtil.isNotBlank(token)) {
            return token;
        }
        if (request instanceof RepeatableRequestWrapper) {
            try {
                String body = new String(((RepeatableRequestWrapper) request).getCachedBody(), StandardCharsets.UTF_8);
                if (StrUtil.isNotBlank(body) && JSONUtil.isTypeJSON(body)) {
                    JSONObject json = JSONUtil.parseObj(body);
                    return json.getStr(tokenName);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
