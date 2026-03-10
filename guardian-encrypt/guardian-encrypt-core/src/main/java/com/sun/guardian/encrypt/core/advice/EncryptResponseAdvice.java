package com.sun.guardian.encrypt.core.advice;

import com.sun.guardian.core.utils.digest.DigestUtils;
import com.sun.guardian.core.utils.ip.IpUtils;
import com.sun.guardian.core.utils.json.GuardianJsonUtils;
import com.sun.guardian.core.utils.log.GuardianLogUtils;
import com.sun.guardian.core.utils.match.MatchUrlRuleUtils;
import com.sun.guardian.encrypt.core.config.encrypt.EncryptConfig;
import com.sun.guardian.encrypt.core.domain.rule.EncryptRule;
import com.sun.guardian.encrypt.core.enums.encrypt.DataEncryptAlgorithm;
import com.sun.guardian.encrypt.core.enums.encrypt.KeyEncryptAlgorithm;
import com.sun.guardian.encrypt.core.enums.mode.DataKeyMode;
import com.sun.guardian.encrypt.core.service.encrypt.DataEncryptService;
import com.sun.guardian.encrypt.core.service.encrypt.KeyEncryptService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 请求加密等返回值
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-09 11:51
 */
@RestControllerAdvice
public class EncryptResponseAdvice implements ResponseBodyAdvice<Object>, Ordered {
    private static final Logger log = LoggerFactory.getLogger(EncryptResponseAdvice.class);
    private static final GuardianLogUtils logUtils = new GuardianLogUtils("[Guardian-Encrypt]", "");

    private final EncryptConfig config;
    private final Map<KeyEncryptAlgorithm, KeyEncryptService> keyEncryptServiceMap;
    private final Map<DataEncryptAlgorithm, DataEncryptService> dataEncryptServiceMap;
    private final int order;

    public EncryptResponseAdvice(EncryptConfig config, Map<KeyEncryptAlgorithm, KeyEncryptService> keyEncryptServiceMap, Map<DataEncryptAlgorithm, DataEncryptService> dataEncryptServiceMap) {
        this.config = config;
        this.keyEncryptServiceMap = keyEncryptServiceMap;
        this.dataEncryptServiceMap = dataEncryptServiceMap;
        this.order = config.getResultAdviceOrder();
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (Objects.isNull(body)) {
            return null;
        }

        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String requestUri = servletRequest.getRequestURI();
        String contextPath = servletRequest.getContextPath();
        String pathWithoutContext = MatchUrlRuleUtils.stripContextPath(requestUri, contextPath);
        String ip = IpUtils.getClientIp(servletRequest);

        if (MatchUrlRuleUtils.matchExcludeUrlRule(config.getExcludeUrls(), requestUri, pathWithoutContext)) {
            logUtils.excludeLog(config.isLogEnabled(), log, requestUri, ip);
            return body;
        }

        EncryptRule rule = MatchUrlRuleUtils.matchUrlRule(config.getUrls(), requestUri, pathWithoutContext);
        if (rule == null) {
            return body;
        }

        logUtils.hitYmlRuleLog(config.isLogEnabled(), log, requestUri, ip);

        String jsonData = GuardianJsonUtils.toJsonStr(body);

        String dataKey;
        if (DataKeyMode.STATIC.equals(config.getData().getKeyMode())) {
            dataKey = config.getData().getKey();
        } else {
            String key = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            dataKey = DigestUtils.base64(key);
        }

        DataEncryptService dataEncryptService = dataEncryptServiceMap.get(rule.getDataAlgorithm());
        String encryptedData = dataEncryptService.encrypt(jsonData, dataKey);

        KeyEncryptService keyEncryptService = keyEncryptServiceMap.get(rule.getKeyAlgorithm());
        String encryptKey = keyEncryptService.encrypt(dataKey, config.getKey().getPublicKey());

        response.getHeaders().add(config.getData().getDataKeyHeader(), encryptKey);

        Map<String, String> result = new LinkedHashMap<>();
        result.put(config.getData().getBodyAlias(), encryptedData);
        return result;
    }
}
