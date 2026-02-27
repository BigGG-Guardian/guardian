package com.sun.guardian.example.antiReplay.controller;

import com.sun.guardian.example.common.CommonResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 防重放攻击示例接口
 *
 * @author scj
 * @since 2026-02-27
 */
@RestController
@RequestMapping("/anti-replay")
public class AntiReplayController {

    /**
     * 受保护接口 — GET 请求
     * <p>
     * 请求头需携带 X-Timestamp 和 X-Nonce，缺失或校验失败将被拦截。
     */
    @GetMapping("/protected/query")
    public CommonResult<String> protectedQuery(@RequestParam(required = false) String keyword) {
        return CommonResult.success("防重放校验通过，查询关键字：" + keyword);
    }

    /**
     * 受保护接口 — POST 请求
     * <p>
     * 模拟支付等敏感操作，必须携带有效的 Timestamp 和 Nonce。
     */
    @PostMapping("/protected/submit")
    public CommonResult<String> protectedSubmit(@RequestBody Map<String, String> body) {
        return CommonResult.success("防重放校验通过，提交数据：" + body);
    }

    /**
     * 白名单接口 — 被 exclude-urls 排除，不受防重放校验
     */
    @GetMapping("/open/health")
    public CommonResult<String> openHealth() {
        return CommonResult.success("此接口不受防重放限制，可直接访问");
    }

    /**
     * 辅助接口 — 生成客户端请求参数（仅供测试使用）
     * <p>
     * 返回当前时间戳和一个随机 Nonce，方便在 Apifox / Postman 中测试。
     */
    @GetMapping("/helper/generate-params")
    public CommonResult<Map<String, String>> generateParams() {
        Map<String, String> params = new java.util.LinkedHashMap<>();
        params.put("X-Timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("X-Nonce", UUID.randomUUID().toString().replace("-", ""));
        return CommonResult.success(params);
    }
}
