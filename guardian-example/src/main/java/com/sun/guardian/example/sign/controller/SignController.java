package com.sun.guardian.example.sign.controller;

import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.sign.core.annotation.SignVerify;
import com.sun.guardian.sign.core.enums.algorithm.SignAlgorithm;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 参数签名示例接口
 *
 * @author scj
 * @since 2026-03-03
 */
@RestController
@RequestMapping("/sign")
public class SignController {

    /**
     * 测试签名验证 — 使用默认算法（BASE64）
     */
    @SignVerify
    @GetMapping("/test")
    public CommonResult<Map<String, String>> testSign(@RequestParam String name, @RequestParam String value) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("value", value);
        return CommonResult.success(result);
    }

    /**
     * 测试 MD5 算法签名验证
     */
    @SignVerify(algorithm = SignAlgorithm.MD5)
    @PostMapping("/md5")
    public CommonResult<Map<String, Object>> testMd5Sign(@RequestBody Map<String, Object> body) {
        return CommonResult.success(body);
    }

    /**
     * 测试 SHA256 算法签名验证
     */
    @SignVerify(algorithm = SignAlgorithm.SHA256)
    @PostMapping("/sha256")
    public CommonResult<Map<String, Object>> testSha256Sign(@RequestBody Map<String, Object> body) {
        return CommonResult.success(body);
    }

    /**
     * 测试 HMAC-SHA256 算法签名验证
     */
    @SignVerify(algorithm = SignAlgorithm.HMAC_SHA256)
    @PostMapping("/hmac-sha256")
    public CommonResult<Map<String, Object>> testHmacSha256Sign(@RequestBody Map<String, Object> body) {
        return CommonResult.success(body);
    }

    /**
     * 测试 SM3 算法签名验证（国密）
     */
    @SignVerify(algorithm = SignAlgorithm.SM3)
    @PostMapping("/sm3")
    public CommonResult<Map<String, Object>> testSm3Sign(@RequestBody Map<String, Object> body) {
        return CommonResult.success(body);
    }

    /**
     * 测试返回值签名
     */
    @SignVerify(algorithm = SignAlgorithm.SHA256)
    @PostMapping("/result-sign")
    public CommonResult<Map<String, Object>> testResultSign(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("request", body);
        result.put("message", "This response is signed");
        return CommonResult.success(result);
    }

    /**
     * 测试 YAML 配置的签名验证
     */
    @GetMapping("/yml/test")
    public CommonResult<Map<String, String>> testYmlSign(@RequestParam String name, @RequestParam String value) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("value", value);
        return CommonResult.success(result);
    }

    /**
     * 测试 YAML 配置的 POST 请求签名验证
     */
    @PostMapping("/yml/post")
    public CommonResult<Map<String, Object>> testYmlPostSign(@RequestBody Map<String, Object> body) {
        return CommonResult.success(body);
    }
}
