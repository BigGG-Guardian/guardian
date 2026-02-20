package com.sun.guardian.core.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sun.guardian.core.wrapper.RepeatableRequestWrapper;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 请求参数工具类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 22:19
 */
public class ArgsUtil {

    private ArgsUtil() {
    }

    /**
     * 从 request 中提取参数，排序后 Base64 编码
     */
    public static String toSortedJsonStr(HttpServletRequest request) {
        JSONObject result = new JSONObject(new TreeMap<>());

        Map<String, String[]> paramMap = request.getParameterMap();
        if (paramMap != null && !paramMap.isEmpty()) {
            TreeMap<String, Object> sorted = new TreeMap<>();
            paramMap.forEach((k, v) -> sorted.put(k, v.length == 1 ? v[0] : v));
            result.set("params", new JSONObject(sorted));
        }

        if (request instanceof RepeatableRequestWrapper) {
            byte[] body = ((RepeatableRequestWrapper) request).getCachedBody();
            if (body != null && body.length > 0) {
                String bodyStr = new String(body, StandardCharsets.UTF_8).trim();
                if (JSONUtil.isTypeJSON(bodyStr)) {
                    result.set("body", toSortedJsonElement(JSONUtil.parse(bodyStr)));
                }
            }
        }

        return result.isEmpty() ? "" : Base64.encode(JSONUtil.toJsonStr(result));
    }

    /**
     * 方法参数数组排序后 Base64 编码
     */
    public static String toSortedJsonStr(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }

        JSONArray jsonArray = new JSONArray();
        for (Object arg : args) {
            if (arg == null || isFilteredType(arg)) {
                continue;
            }
            Object sorted = toSortedJsonElement(arg);
            jsonArray.add(sorted);
        }

        if (jsonArray.isEmpty()) {
            return "";
        }
        String json = jsonArray.size() == 1
                ? JSONUtil.toJsonStr(jsonArray.get(0))
                : JSONUtil.toJsonStr(jsonArray);
        return Base64.encode(json);
    }

    /**
     * 解析 JSON 字符串并递归 trim 所有字符串值
     */
    public static String trimJsonBody(String json, Set<String> excludeFields,
                                      CharacterSanitizer sanitizer) {
        if (StrUtil.isBlank(json)) return json;
        Object parsed = JSONUtil.parse(json);
        trimRecursive(parsed, excludeFields, sanitizer);
        return JSONUtil.toJsonStr(parsed);
    }

    /**
     * 递归遍历 JSON 结构，对字符串值执行 sanitize + trim
     */
    private static void trimRecursive(Object obj, Set<String> excludeFields,
                                      CharacterSanitizer sanitizer) {
        if (obj instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) obj;
            jsonObj.forEach((key, value) -> {
                if (value instanceof String) {
                    if (!excludeFields.contains(key)) {
                        jsonObj.put(key, sanitizer.sanitize((String) value).trim());
                    }
                } else if (value instanceof JSONObject || value instanceof JSONArray) {
                    trimRecursive(value, excludeFields, sanitizer);
                }
            });
        } else if (obj instanceof JSONArray) {
            JSONArray jsonArr = (JSONArray) obj;
            for (int i = 0; i < jsonArr.size(); i++) {
                Object item = jsonArr.get(i);
                if (item instanceof String) {
                    jsonArr.set(i, sanitizer.sanitize((String) item).trim());
                } else if (item instanceof JSONObject || item instanceof JSONArray) {
                    trimRecursive(item, excludeFields, sanitizer);
                }
            }
        }
    }

    /**
     * 递归转为字段按字典序排列的 JSON
     */
    private static Object toSortedJsonElement(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof CharSequence || obj instanceof Number
                || obj instanceof Boolean || obj instanceof Character) {
            return obj;
        }

        if (obj instanceof Collection || obj.getClass().isArray()) {
            JSONArray srcArray = JSONUtil.parseArray(JSONUtil.toJsonStr(obj));
            JSONArray sortedArray = new JSONArray();
            for (Object element : srcArray) {
                sortedArray.add(toSortedJsonElement(element));
            }
            return sortedArray;
        }

        JSONObject srcObj;
        if (obj instanceof JSONObject) {
            srcObj = (JSONObject) obj;
        } else {
            srcObj = JSONUtil.parseObj(JSONUtil.toJsonStr(obj));
        }
        return sortJsonObject(srcObj);
    }

    /**
     * key 按字典序排序
     */
    private static JSONObject sortJsonObject(JSONObject jsonObject) {
        Map<String, Object> sortedMap = new TreeMap<>();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            sortedMap.put(entry.getKey(), toSortedJsonElement(entry.getValue()));
        }
        return new JSONObject(sortedMap);
    }

    /**
     * 判断是否为不可序列化类型
     */
    private static boolean isFilteredType(Object obj) {
        return obj instanceof ServletRequest
                || obj instanceof ServletResponse
                || obj instanceof InputStream
                || obj instanceof OutputStream
                || obj instanceof MultipartFile;
    }
}
