package com.sun.guardian.sign.core.advice;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.guardian.core.utils.args.ArgsUtils;
import com.sun.guardian.core.utils.json.GuardianJsonUtils;
import com.sun.guardian.sign.core.config.SignConfig;
import com.sun.guardian.sign.core.enums.algorithm.SignAlgorithm;
import com.sun.guardian.sign.core.service.sign.SignService;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 参数签名返回值签名
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-03
 */
@ControllerAdvice
public class SignResultSignAdvice implements ResponseBodyAdvice<Object> {
    public static final String RESPONSE_SIGN_ALGORITHM_ATTRIBUTE = "guardian_sign_result_algorithm";
    private final SignConfig signConfig;
    private final SignService signService;

    public SignResultSignAdvice(SignConfig signConfig, SignService signService) {
        this.signConfig = signConfig;
        this.signService = signService;
    }

    /**
     * 判断是否支持返回值缓存
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 写入响应体前写入签名
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        if (!(request instanceof ServletServerHttpRequest)) {
            return body;
        }
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        SignAlgorithm algorithm = (SignAlgorithm) servletRequest.getAttribute(RESPONSE_SIGN_ALGORITHM_ATTRIBUTE);

        if (algorithm == null) {
            return body;
        }

        SortedMap<String, String> params = ArgsUtils.toSorted(body);

        long timestamp = System.currentTimeMillis();

        String sign = signService.sign(params, String.valueOf(timestamp), signConfig.getSecretKey(), algorithm);

        response.getHeaders().add(signConfig.getSignHeader(), sign);
        response.getHeaders().add(signConfig.getTimestampHeader(), String.valueOf(timestamp));

        return body;
    }


}
