package com.sun.guardian.core.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON 工具类（基于 Jackson）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-25
 */
public class GuardianJsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GuardianJsonUtils() {
    }

    public static String toJsonStr(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON serialize failed", e);
        }
    }

    public static <T> T toBean(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON deserialize failed", e);
        }
    }

    public static JsonNode readTree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON parse failed", e);
        }
    }

    public static boolean isJson(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            MAPPER.readTree(str);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public static ObjectNode createObjectNode() {
        return MAPPER.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return MAPPER.createArrayNode();
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
