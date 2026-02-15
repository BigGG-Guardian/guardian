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
}
