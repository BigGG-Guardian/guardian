package com.sun.guardian.example.autoTrim.controller;

import com.sun.guardian.example.common.CommonResult;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求参数自动 trim 示例接口
 *
 * @author scj
 * @since 2026-02-20
 */
@RestController
@RequestMapping("/auto-trim")
public class AutoTrimController {

    /**
     * 表单参数 trim — GET 请求，query 参数前后空格自动去除
     */
    @GetMapping("/form")
    public CommonResult<Map<String, String>> formTrim(@RequestParam String name,
                                                      @RequestParam String email) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("email", email);
        return CommonResult.success(result);
    }

    /**
     * JSON Body trim — POST 请求，JSON 字段值前后空格自动去除
     */
    @PostMapping("/json")
    public CommonResult<Map<String, Object>> jsonTrim(@RequestBody Map<String, Object> body) {
        return CommonResult.success(body);
    }

    /**
     * 排除字段验证 — password 字段配置在 exclude-fields 中，不会被 trim
     */
    @PostMapping("/exclude")
    public CommonResult<Map<String, Object>> excludeField(@RequestParam String username,
                                                          @RequestParam String password) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("username", username);
        result.put("password", password);
        return CommonResult.success(result);
    }

    /**
     * JSON 排除字段验证 — JSON body 中 signature 字段不会被 trim
     */
    @PostMapping("/json-exclude")
    public CommonResult<Map<String, Object>> jsonExcludeField(@RequestBody Map<String, Object> body) {
        return CommonResult.success(body);
    }

    /**
     * 特殊字符替换验证 — 零宽空格 (\u200B) 和 BOM (\uFEFF) 会被替换为空字符串
     */
    @PostMapping("/sanitize")
    public CommonResult<Map<String, Object>> sanitize(@RequestBody Map<String, Object> body) {
        return CommonResult.success(body);
    }

    /**
     * 综合验证 — 表单参数 trim + 排除字段 + 特殊字符替换同时生效
     */
    @PostMapping("/mixed")
    public CommonResult<Map<String, String>> mixed(@RequestParam String name,
                                                   @RequestParam String password,
                                                   @RequestParam String code) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("password", password);
        result.put("code", code);
        return CommonResult.success(result);
    }
}
