package com.sun.guardian.core.utils.args;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.guardian.core.utils.json.GuardianJsonUtils;
import com.sun.guardian.core.utils.string.CharacterSanitizer;
import com.sun.guardian.core.wrapper.RepeatableRequestWrapper;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 请求参数工具类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 22:19
 */
public class ArgsUtils {

    private ArgsUtils() {
    }

    /**
     * 从 request 中提取参数，排序后 Base64 编码
     */
    public static String toSortedJsonStr(HttpServletRequest request) {
        ObjectNode result = GuardianJsonUtils.createObjectNode();

        Map<String, String[]> paramMap = request.getParameterMap();
        if (paramMap != null && !paramMap.isEmpty()) {
            TreeMap<String, Object> sorted = new TreeMap<>();
            paramMap.forEach((k, v) -> sorted.put(k, v.length == 1 ? v[0] : v));
            result.set("params", GuardianJsonUtils.getMapper().valueToTree(sorted));
        }

        if (request instanceof RepeatableRequestWrapper) {
            byte[] body = ((RepeatableRequestWrapper) request).getCachedBody();
            if (body != null && body.length > 0) {
                String bodyStr = new String(body, StandardCharsets.UTF_8).trim();
                if (GuardianJsonUtils.isJson(bodyStr)) {
                    result.set("body", toSortedJsonNode(GuardianJsonUtils.readTree(bodyStr)));
                }
            }
        }

        if (result.size() == 0) {
            return "";
        }
        return Base64.getEncoder().encodeToString(
                GuardianJsonUtils.toJsonStr(result).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 方法参数数组排序后 Base64 编码
     */
    public static String toSortedJsonStr(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }

        ArrayNode jsonArray = GuardianJsonUtils.createArrayNode();
        for (Object arg : args) {
            if (arg == null || isFilteredType(arg)) {
                continue;
            }
            JsonNode node = GuardianJsonUtils.getMapper().valueToTree(arg);
            jsonArray.add(toSortedJsonNode(node));
        }

        if (jsonArray.size() == 0) {
            return "";
        }
        String json = jsonArray.size() == 1
                ? GuardianJsonUtils.toJsonStr(jsonArray.get(0))
                : GuardianJsonUtils.toJsonStr(jsonArray);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析 JSON 字符串并递归 trim 所有字符串值
     */
    public static String trimJsonBody(String json, Set<String> excludeFields,
                                      CharacterSanitizer sanitizer) {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }
        JsonNode parsed = GuardianJsonUtils.readTree(json);
        JsonNode trimmed = trimRecursive(parsed, excludeFields, sanitizer);
        return GuardianJsonUtils.toJsonStr(trimmed);
    }

    /**
     * 递归遍历 JSON 结构，对字符串值执行 sanitize + trim
     */
    private static JsonNode trimRecursive(JsonNode node, Set<String> excludeFields,
                                          CharacterSanitizer sanitizer) {
        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (value.isTextual()) {
                    if (!excludeFields.contains(key)) {
                        objNode.put(key, sanitizer.sanitize(value.asText()).trim());
                    }
                } else if (value.isObject() || value.isArray()) {
                    trimRecursive(value, excludeFields, sanitizer);
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrNode = (ArrayNode) node;
            for (int i = 0; i < arrNode.size(); i++) {
                JsonNode item = arrNode.get(i);
                if (item.isTextual()) {
                    arrNode.set(i, TextNode.valueOf(sanitizer.sanitize(item.asText()).trim()));
                } else if (item.isObject() || item.isArray()) {
                    trimRecursive(item, excludeFields, sanitizer);
                }
            }
        }
        return node;
    }

    /**
     * 递归转为字段按字典序排列的 JSON
     */
    private static JsonNode toSortedJsonNode(JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return node;
        }
        if (node.isArray()) {
            ArrayNode sortedArray = GuardianJsonUtils.createArrayNode();
            for (JsonNode element : node) {
                sortedArray.add(toSortedJsonNode(element));
            }
            return sortedArray;
        }
        if (node.isObject()) {
            ObjectNode sortedObj = GuardianJsonUtils.createObjectNode();
            TreeMap<String, JsonNode> sorted = new TreeMap<>();
            node.fields().forEachRemaining(e -> sorted.put(e.getKey(), toSortedJsonNode(e.getValue())));
            sorted.forEach(sortedObj::set);
            return sortedObj;
        }
        return node;
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
