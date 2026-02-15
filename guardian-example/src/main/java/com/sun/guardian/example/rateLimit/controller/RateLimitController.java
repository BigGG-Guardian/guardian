package com.sun.guardian.example.rateLimit.controller;

import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.rate.limit.core.annotation.RateLimit;
import com.sun.guardian.rate.limit.core.enums.algorithm.RateLimitAlgorithm;
import com.sun.guardian.rate.limit.core.enums.scope.RateLimitKeyScope;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 限流示例接口
 * <p>
 * 滑动窗口：maxCount = qps × window(秒)
 * <br>令牌桶：补充速率 = qps / window(秒)，capacity 为桶容量
 *
 * @author scj
 * @since 2026-02-09
 */
@RestController
@RequestMapping("/rate-limit")
public class RateLimitController {

    // ==================== YAML 规则（滑动窗口） ====================

    /**
     * YAML 全局限流 — 搜索接口
     * <p>
     * qps=2, window=1s, global → 每秒 2 次，快速请求 3 次触发
     */
    @GetMapping("/yml/search")
    public CommonResult<String> ymlSearch(@RequestParam(defaultValue = "guardian") String keyword) {
        return CommonResult.success("搜索成功: " + keyword);
    }

    /**
     * YAML IP 限流 — 短信接口
     * <p>
     * qps=1, window=1s, ip → 同一 IP 每秒 1 次
     */
    @PostMapping("/yml/sms")
    public CommonResult<String> ymlSms(@RequestBody Map<String, String> body) {
        return CommonResult.success("短信发送成功，参数：" + body);
    }

    /**
     * YAML 令牌桶 — 秒杀接口
     * <p>
     * qps=2, capacity=5, token_bucket, global → 初始 5 个令牌可突发，之后 0.5s 恢复 1 个
     */
    @PostMapping("/yml/seckill")
    public CommonResult<String> ymlSeckill() {
        return CommonResult.success("秒杀接口调用成功（令牌桶）");
    }

    /**
     * YAML 白名单 — exclude-urls 命中，不限流
     */
    @GetMapping("/yml/whitelist")
    public CommonResult<String> ymlWhitelist() {
        return CommonResult.success("白名单接口，不受限流限制");
    }

    // ==================== 注解（滑动窗口） ====================

    /**
     * 全局限流 — qps=2, 1s 窗口，快速请求 3 次触发
     */
    @RateLimit(qps = 2, message = "接口请求过于频繁，请稍后重试")
    @GetMapping("/annotation/global")
    public CommonResult<String> annotationGlobal() {
        return CommonResult.success("全局限流接口调用成功");
    }

    /**
     * IP 限流 — qps=2, 5s 窗口 → 5s 内同一 IP 最多 10 次（2×5）
     */
    @RateLimit(qps = 2, window = 5, windowUnit = TimeUnit.SECONDS, rateLimitScope = RateLimitKeyScope.IP,
            message = "您的 IP 请求过于频繁")
    @GetMapping("/annotation/ip")
    public CommonResult<String> annotationIp() {
        return CommonResult.success("IP 限流接口调用成功");
    }

    /**
     * 用户限流 — qps=3, 5s 窗口 → 5s 内同一用户最多 15 次（3×5）
     * <br>示例 UserContext 返回 null，降级为 sessionId/IP
     */
    @RateLimit(qps = 3, window = 5, windowUnit = TimeUnit.SECONDS, rateLimitScope = RateLimitKeyScope.USER,
            message = "您的操作过于频繁，请稍后重试")
    @GetMapping("/annotation/user")
    public CommonResult<String> annotationUser() {
        return CommonResult.success("用户限流接口调用成功");
    }

    /**
     * 极低 QPS — qps=1, 1s，连续请求 2 次即触发
     */
    @RateLimit(qps = 1, message = "系统繁忙，请稍后重试")
    @PostMapping("/annotation/strict")
    public CommonResult<String> annotationStrict(@RequestBody Map<String, String> body) {
        return CommonResult.success("严格限流接口调用成功，参数：" + body);
    }

    /**
     * 分钟级窗口 — qps=1, 1min, IP → 60s 内同一 IP 最多 60 次
     */
    @RateLimit(qps = 1, window = 1, windowUnit = TimeUnit.MINUTES, rateLimitScope = RateLimitKeyScope.IP,
            message = "请求过于频繁，请稍后再试")
    @PostMapping("/annotation/minute")
    public CommonResult<String> annotationMinute() {
        return CommonResult.success("分钟级限流接口调用成功");
    }

    // ==================== 注解（令牌桶） ====================

    /**
     * 基本令牌桶 — qps=2, capacity=2（默认），初始 2 个令牌，第 3 次被拒
     */
    @RateLimit(qps = 2, algorithm = RateLimitAlgorithm.TOKEN_BUCKET,
            message = "令牌不足，请稍后重试")
    @GetMapping("/annotation/token-bucket/basic")
    public CommonResult<String> tokenBucketBasic() {
        return CommonResult.success("令牌桶基本限流接口调用成功");
    }

    /**
     * 突发令牌桶 — qps=2, capacity=10，初始 10 个令牌可一次打完，之后 0.5s 恢复 1 个
     */
    @RateLimit(qps = 2, capacity = 10, algorithm = RateLimitAlgorithm.TOKEN_BUCKET,
            message = "突发额度已用完，请稍后重试")
    @GetMapping("/annotation/token-bucket/burst")
    public CommonResult<String> tokenBucketBurst() {
        return CommonResult.success("令牌桶突发限流接口调用成功");
    }

    /**
     * IP 令牌桶 — qps=1, capacity=3, IP 维度，每个 IP 初始 3 个，用完后每秒恢复 1 个
     */
    @RateLimit(qps = 1, capacity = 3, algorithm = RateLimitAlgorithm.TOKEN_BUCKET,
            rateLimitScope = RateLimitKeyScope.IP,
            message = "您的 IP 令牌已耗尽，请稍后重试")
    @GetMapping("/annotation/token-bucket/ip")
    public CommonResult<String> tokenBucketIp() {
        return CommonResult.success("令牌桶 IP 限流接口调用成功");
    }

    /**
     * 分钟级令牌桶 — qps=5/min, capacity=5，初始 5 个用完后约 12 秒恢复 1 个
     */
    @RateLimit(qps = 5, window = 1, windowUnit = TimeUnit.MINUTES, capacity = 5,
            algorithm = RateLimitAlgorithm.TOKEN_BUCKET,
            message = "令牌补充中，请稍后重试")
    @GetMapping("/annotation/token-bucket/slow-refill")
    public CommonResult<String> tokenBucketSlowRefill() {
        return CommonResult.success("分钟级令牌桶接口调用成功");
    }
}
