package com.sun.guardian.example.encrypt.controller;

import com.sun.guardian.example.common.CommonResult;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求加密解密示例接口
 *
 * @author scj
 * @since 2026-03-09
 */
@RestController
@RequestMapping("/encrypt")
public class EncryptController {

    // ==================== 解密测试接口 ====================

    /**
     * 测试解密 - 仅 params
     */
    @GetMapping("/decrypt/params")
    public CommonResult<Map<String, Object>> testDecryptParams(@RequestParam String name,
                                                               @RequestParam String value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("value", value);
        result.put("type", "params");
        return CommonResult.success(result);
    }

    /**
     * 测试解密 - 仅 body
     */
    @PostMapping("/decrypt/body")
    public CommonResult<Map<String, Object>> testDecryptBody(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", body);
        result.put("type", "body");
        return CommonResult.success(result);
    }

    /**
     * 测试解密 - params + body
     */
    @PostMapping("/decrypt/mix")
    public CommonResult<Map<String, Object>> testDecryptMix(@RequestParam String userId,
                                                            @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("body", body);
        result.put("type", "mix");
        return CommonResult.success(result);
    }

    // ==================== 加密测试接口 ====================

    /**
     * 测试加密 - 返回对象
     */
    @GetMapping("/encrypt/object")
    public CommonResult<Map<String, Object>> testEncryptObject() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", "test");
        result.put("value", 123);
        result.put("timestamp", System.currentTimeMillis());
        return CommonResult.success(result);
    }

    /**
     * 测试加密 - 返回字符串
     */
    @GetMapping("/encrypt/string")
    public CommonResult<String> testEncryptString() {
        return CommonResult.success("Hello, Guardian!");
    }

    /**
     * 测试加密 - 返回 null
     */
    @GetMapping("/encrypt/null")
    public CommonResult<Object> testEncryptNull() {
        return CommonResult.success(null);
    }

    /**
     * 测试加密 - 返回空对象
     */
    @GetMapping("/encrypt/empty")
    public CommonResult<Object> testEncryptEmpty() {
        return CommonResult.success(new LinkedHashMap<>());
    }

    /**
     * 测试加密 - 返回数组
     */
    @GetMapping("/encrypt/array")
    public CommonResult<Object[]> testEncryptArray() {
        return CommonResult.success(new Object[]{"item1", "item2", 123});
    }

    /**
     * 测试加密 - 返回嵌套对象
     */
    @GetMapping("/encrypt/nested")
    public CommonResult<Map<String, Object>> testEncryptNested() {
        Map<String, Object> level3 = new LinkedHashMap<>();
        level3.put("level3", "deepValue");
        
        Map<String, Object> level2 = new LinkedHashMap<>();
        level2.put("level2", level3);
        
        Map<String, Object> level1 = new LinkedHashMap<>();
        level1.put("level1", level2);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nested", level1);
        result.put("simple", "value");
        return CommonResult.success(result);
    }

    // ==================== 加密 + 解密测试接口 ====================

    /**
     * 测试加密解密 - POST 请求
     */
    @PostMapping("/both/post")
    public CommonResult<Map<String, Object>> testBothPost(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("received", body);
        result.put("processed", true);
        result.put("timestamp", System.currentTimeMillis());
        return CommonResult.success(result);
    }

    /**
     * 测试加密解密 - params + body
     */
    @PostMapping("/both/mix")
    public CommonResult<Map<String, Object>> testBothMix(@RequestParam String action,
                                                         @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("action", action);
        result.put("body", body);
        result.put("processed", true);
        return CommonResult.success(result);
    }

    /**
     * 测试加密解密 - 返回 null
     */
    @PostMapping("/both/null")
    public CommonResult<Object> testBothNull(@RequestBody Map<String, Object> body) {
        return CommonResult.success(null);
    }

    /**
     * 测试加密解密 - 返回空 Map
     */
    @PostMapping("/both/empty")
    public CommonResult<Object> testBothEmpty(@RequestBody Map<String, Object> body) {
        return CommonResult.success(new LinkedHashMap<>());
    }

    // ==================== 边界测试接口 ====================

    /**
     * 测试 - 大数据量
     */
    @PostMapping("/large")
    public CommonResult<Map<String, Object>> testLarge(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inputSize", body.size());
        result.put("data", body);
        return CommonResult.success(result);
    }

    /**
     * 测试 - 特殊字符
     */
    @PostMapping("/special-chars")
    public CommonResult<Map<String, Object>> testSpecialChars(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("specialChars", "!@#$%^&*()_+-=[]{}|;':\",./<>?");
        result.put("chinese", "中文测试");
        result.put("emoji", "😀🎉");
        result.put("input", body);
        return CommonResult.success(result);
    }

    /**
     * 测试 - 无需加密解密（不在配置的 URL 中）
     */
    @GetMapping("/normal")
    public CommonResult<String> testNormal() {
        return CommonResult.success("This is a normal response without encryption");
    }
}
