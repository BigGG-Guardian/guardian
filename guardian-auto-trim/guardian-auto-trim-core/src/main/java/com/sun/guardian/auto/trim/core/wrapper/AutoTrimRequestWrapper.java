package com.sun.guardian.auto.trim.core.wrapper;

import cn.hutool.core.io.IoUtil;
import com.sun.guardian.core.utils.ArgsUtil;
import com.sun.guardian.core.utils.CharacterSanitizer;
import com.sun.guardian.core.wrapper.RepeatableRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 参数自动 trim 请求包装器，excludeFields 统一作用于表单参数和 JSON body 字段
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 14:40
 */
public class AutoTrimRequestWrapper extends RepeatableRequestWrapper {

    private final Set<String> excludeFields;
    private final CharacterSanitizer sanitizer;
    private volatile Map<String, String[]> cachedParameterMap;

    /**
     * 构造 trim 包装器，读取并处理请求体
     */
    public AutoTrimRequestWrapper(HttpServletRequest request, Set<String> excludeFields,
                                  CharacterSanitizer sanitizer) throws IOException {
        super(request, processBody(request, excludeFields, sanitizer));
        this.excludeFields = excludeFields;
        this.sanitizer = sanitizer;
    }

    /**
     * 处理请求体，JSON 请求执行递归 trim，非 JSON 请求原样返回
     */
    private static byte[] processBody(HttpServletRequest request, Set<String> excludeFields,
                                      CharacterSanitizer sanitizer) throws IOException {
        byte[] rawBody;
        if (request instanceof RepeatableRequestWrapper) {
            rawBody = ((RepeatableRequestWrapper) request).getCachedBody();
        } else {
            rawBody = IoUtil.readBytes(request.getInputStream());
        }
        if (rawBody.length == 0) return rawBody;

        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            String json = new String(rawBody, StandardCharsets.UTF_8);
            json = ArgsUtil.trimJsonBody(json, excludeFields, sanitizer);
            return json.getBytes(StandardCharsets.UTF_8);
        }
        return rawBody;
    }

    /**
     * 获取单个参数值，排除字段不做 trim
     */
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value == null || excludeFields.contains(name)) return value;
        return sanitizer.sanitize(value).trim();
    }

    /**
     * 获取参数值数组，排除字段不做 trim
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        if (excludeFields.contains(name)) return values;
        return Arrays.stream(values)
                .map(v -> v == null ? null : sanitizer.sanitize(v).trim())
                .toArray(String[]::new);
    }

    /**
     * 获取全部参数 Map，结果缓存避免重复计算
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        if (cachedParameterMap != null) return cachedParameterMap;
        Map<String, String[]> original = super.getParameterMap();
        Map<String, String[]> trimmed = new LinkedHashMap<>();
        original.forEach((key, values) -> {
            if (excludeFields.contains(key)) {
                trimmed.put(key, values);
            } else {
                trimmed.put(key, Arrays.stream(values)
                        .map(v -> v == null ? null : sanitizer.sanitize(v).trim())
                        .toArray(String[]::new));
            }
        });
        cachedParameterMap = Collections.unmodifiableMap(trimmed);
        return cachedParameterMap;
    }
}
