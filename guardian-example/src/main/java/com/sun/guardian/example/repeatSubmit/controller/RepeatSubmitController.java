package com.sun.guardian.example.repeatSubmit.controller;

import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.repeat.submit.core.annotation.RepeatSubmit;
import com.sun.guardian.repeat.submit.core.enums.scope.KeyScope;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交示例接口
 *
 * @author scj
 * @since 2026-02-13 10:53
 */
@RestController
@RequestMapping("/repeat-submit")
public class RepeatSubmitController {

    // ==================== YAML 规则防重 ====================

    /**
     * YAML 白名单 — exclude-urls 命中，不受防重限制
     */
    @GetMapping("/yml/whitelist")
    public CommonResult<String> ymlWhitelist(@RequestParam Map<String, String> params) {
        return CommonResult.success("白名单接口，不受防重限制，参数：" + params);
    }

    /**
     * YAML POST 防重 — 匹配 /repeat-submit/yml/**，interval=10s, user 维度
     */
    @PostMapping("/yml/basic")
    public CommonResult<String> ymlBasic(@RequestBody Map<String, String> body) {
        return CommonResult.success("YAML 防重接口调用成功，参数：" + body);
    }

    /**
     * YAML GET 防重 — 同上规则，GET 请求
     */
    @GetMapping("/yml/get")
    public CommonResult<String> ymlGet(@RequestParam Map<String, String> params) {
        return CommonResult.success("YAML GET 防重接口调用成功，参数：" + params);
    }

    // ==================== YAML SpEL 形式防重 ====================

    /**
     * YAML SpEL - 仅 params（URL参数）
     * sp-el: "#orderId" 在 yml 中配置
     */
    @GetMapping("/yml/spel-params")
    public CommonResult<String> ymlSpelParams(@RequestParam String orderId) {
        return CommonResult.success("YAML SpEL-Params 防重接口调用成功，orderId=" + orderId);
    }

    /**
     * YAML SpEL - 仅 body（JSON请求体）
     * sp-el: "#orderId" 在 yml 中配置
     */
    @PostMapping("/yml/spel-body")
    public CommonResult<String> ymlSpelBody(@RequestBody Map<String, String> body) {
        return CommonResult.success("YAML SpEL-Body 防重接口调用成功，body=" + body);
    }

    /**
     * YAML SpEL - params + body 混合
     * sp-el: "#userId + ':' + #body.orderId" 在 yml 中配置
     */
    @PostMapping("/yml/spel-mix")
    public CommonResult<String> ymlSpelMix(@RequestParam String userId, @RequestBody Map<String, String> body) {
        return CommonResult.success("YAML SpEL-Mix 防重接口调用成功，userId=" + userId + ", body=" + body);
    }

    // ==================== 注解方式防重 ====================

    /**
     * 用户维度防重 — interval=10s, keyScope=USER（默认）
     */
    @RepeatSubmit(interval = 10, message = "请勿重复提交（用户维度）")
    @PostMapping("/annotation/user-scope")
    public CommonResult<String> annotationUserScope(@RequestBody Map<String, String> body) {
        return CommonResult.success("用户维度防重接口调用成功，参数：" + body);
    }

    /**
     * IP 维度防重 — interval=10s, keyScope=IP，不区分用户
     */
    @RepeatSubmit(interval = 10, keyScope = KeyScope.IP, message = "请勿重复提交（IP 维度）")
    @PostMapping("/annotation/ip-scope")
    public CommonResult<String> annotationIpScope(@RequestBody Map<String, String> body) {
        return CommonResult.success("IP 维度防重接口调用成功，参数：" + body);
    }

    /**
     * 全局维度防重 — interval=10s, keyScope=GLOBAL，不区分用户和 IP
     */
    @RepeatSubmit(interval = 10, keyScope = KeyScope.GLOBAL, message = "请勿重复提交（全局维度）")
    @PostMapping("/annotation/global-scope")
    public CommonResult<String> annotationGlobalScope(@RequestBody Map<String, String> body) {
        return CommonResult.success("全局维度防重接口调用成功，参数：" + body);
    }

    /**
     * 长窗口防重 — interval=30s
     */
    @RepeatSubmit(interval = 30, timeUnit = TimeUnit.SECONDS, message = "订单正在处理，请 30 秒后再试")
    @PostMapping("/annotation/long-interval")
    public CommonResult<String> annotationLongInterval(@RequestBody Map<String, String> body) {
        return CommonResult.success("长窗口防重接口调用成功，参数：" + body);
    }

    /**
     * 异常自动释放锁 — 业务异常时 Guardian 自动释放防重锁，后续请求不会被误拦
     */
    @RepeatSubmit(interval = 10, message = "请勿重复提交")
    @PostMapping("/annotation/exception-release")
    public CommonResult<String> annotationExceptionRelease(@RequestBody Map<String, String> body) {
        throw new RuntimeException("模拟业务异常，验证防重锁自动释放");
    }

    // ==================== SpEL 形式防重 ====================

    /**
     * SpEL - 仅 params（URL参数）
     * spEl = "#orderId" 表示从请求参数中取 orderId
     */
    @RepeatSubmit(interval = 10, spEl = "#orderId", message = "请勿重复提交订单")
    @GetMapping("/spel/params")
    public CommonResult<String> spelParams(@RequestParam String orderId) {
        return CommonResult.success("SpEL-Params 防重接口调用成功，orderId=" + orderId);
    }

    /**
     * SpEL - 仅 body（JSON请求体）
     * spEl = "#orderId" 表示从请求体中取 orderId（只有body时，直接用#字段名）
     */
    @RepeatSubmit(interval = 10, spEl = "#orderId", message = "请勿重复提交订单")
    @PostMapping("/spel/body")
    public CommonResult<String> spelBody(@RequestBody Map<String, String> body) {
        return CommonResult.success("SpEL-Body 防重接口调用成功，body=" + body);
    }

    /**
     * SpEL - params + body 混合
     * spEl = "#userId + ':' + #body.orderId" 表示组合 userId 和 orderId
     * 其中 #userId 来自 URL 参数，#body.orderId 来自请求体（需加body前缀区分）
     */
    @RepeatSubmit(interval = 10, spEl = "#userId + ':' + #body.orderId", message = "请勿重复提交订单")
    @PostMapping("/spel/mix")
    public CommonResult<String> spelMix(@RequestParam String userId, @RequestBody Map<String, String> body) {
        return CommonResult.success("SpEL-Mix 防重接口调用成功，userId=" + userId + ", body=" + body);
    }
}
