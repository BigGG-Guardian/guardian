package com.sun.guardian.encrypt.core.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.guardian.core.exception.DecryptException;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.core.utils.args.ArgsUtils;
import com.sun.guardian.core.utils.ip.IpUtils;
import com.sun.guardian.core.utils.json.GuardianJsonUtils;
import com.sun.guardian.core.utils.log.GuardianLogUtils;
import com.sun.guardian.core.utils.match.MatchUrlRuleUtils;
import com.sun.guardian.core.utils.response.ResponseUtils;
import com.sun.guardian.encrypt.core.config.decrypt.DecryptConfig;
import com.sun.guardian.encrypt.core.domain.rule.EncryptRule;
import com.sun.guardian.encrypt.core.enums.encrypt.DataEncryptAlgorithm;
import com.sun.guardian.encrypt.core.enums.encrypt.KeyEncryptAlgorithm;
import com.sun.guardian.encrypt.core.service.decrypt.DataDecryptService;
import com.sun.guardian.encrypt.core.service.decrypt.KeyDecryptService;
import com.sun.guardian.encrypt.core.service.response.EncryptResponseHandler;
import com.sun.guardian.encrypt.core.wrapper.EncryptRequestWrapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 请求解密 过滤器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 19:38
 */
public class DecryptFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(DecryptFilter.class);
    private static final GuardianLogUtils logUtils = new GuardianLogUtils("[Guardian-Decrypt]", "");

    private final DecryptConfig decryptConfig;
    private final ResponseUtils responseUtils;
    private final Map<KeyEncryptAlgorithm, KeyDecryptService> keyDecryptServiceMap;
    private final Map<DataEncryptAlgorithm, DataDecryptService> dataDecryptServiceMap;

    public DecryptFilter(DecryptConfig decryptConfig, Map<KeyEncryptAlgorithm, KeyDecryptService> keyDecryptServiceMap, Map<DataEncryptAlgorithm, DataDecryptService> dataDecryptServiceMap, EncryptResponseHandler responseHandler, GuardianMessageResolver messageResolver) {
        this.decryptConfig = decryptConfig;
        this.keyDecryptServiceMap = keyDecryptServiceMap;
        this.dataDecryptServiceMap = dataDecryptServiceMap;
        this.responseUtils = new ResponseUtils(decryptConfig, responseHandler, DecryptException::new, messageResolver);
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathWithoutContext = MatchUrlRuleUtils.stripContextPath(requestUri, contextPath);
        String ip = IpUtils.getClientIp(request);

        if (MatchUrlRuleUtils.matchExcludeUrlRule(decryptConfig.getExcludeUrls(), requestUri, pathWithoutContext)) {
            logUtils.excludeLog(decryptConfig.isLogEnabled(), log, requestUri, ip);
            filterChain.doFilter(request, response);
            return;
        }

        EncryptRule rule = MatchUrlRuleUtils.matchUrlRule(decryptConfig.getUrls(), requestUri, pathWithoutContext);
        if (rule == null) {
            logUtils.passLog(decryptConfig.isLogEnabled(), log, requestUri, ip);
            filterChain.doFilter(request, response);
            return;
        }

        String keyEncryptStr = request.getHeader(decryptConfig.getData().getDataKeyHeader());
        if (!StringUtils.hasText(keyEncryptStr)) {
            logUtils.blockLog(decryptConfig.isLogEnabled(), log, requestUri, ip);
            responseUtils.reject(request, response, decryptConfig.getMissingDataKeyHeaderMessage());
            return;
        }

        KeyDecryptService keyDecryptService = keyDecryptServiceMap.get(rule.getKeyAlgorithm());
        String keyStr = keyDecryptService.decrypt(keyEncryptStr, decryptConfig.getKey().getPrivateKey());

        DataDecryptService dataDecryptService = dataDecryptServiceMap.get(rule.getDataAlgorithm());

        EncryptRequestWrapper wrapper = new EncryptRequestWrapper(request);
        String encryptParams = request.getParameter(decryptConfig.getData().getParamAlias());
        if (StringUtils.hasText(encryptParams)) {
            String decryptedParams = dataDecryptService.decrypt(encryptParams, keyStr);
            Map<String, String[]> paramsMap = ArgsUtils.parseParams(decryptedParams);
            wrapper.setParameters(paramsMap);
        }

        byte[] cachedBody = wrapper.getCachedBody();
        if (cachedBody != null && cachedBody.length > 0) {
            String bodyStr = new String(cachedBody, StandardCharsets.UTF_8);
            JsonNode node = GuardianJsonUtils.readTree(bodyStr);
            JsonNode dataNode = node.get(decryptConfig.getData().getBodyAlias());

            if (dataNode != null && StringUtils.hasText(dataNode.asText())) {
                String decryptedBody = dataDecryptService.decrypt(dataNode.asText(), keyStr);
                wrapper.setCachedBody(decryptedBody.getBytes(StandardCharsets.UTF_8));
            }
        }

        logUtils.passLog(decryptConfig.isLogEnabled(), log, requestUri, ip);
        filterChain.doFilter(wrapper, response);
    }
}
