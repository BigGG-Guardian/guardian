package com.sun.guardian.example.idempotent.controller;

import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.idempotent.core.annotation.Idempotent;
import com.sun.guardian.idempotent.core.enums.IdempotentTokenFrom;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 接口幂等示例接口
 * <p>
 * 获取 Token：GET /guardian/idempotent/token?key={接口标识}
 *
 * @author scj
 * @since 2026-02-19
 */
@RestController
@RequestMapping("/idempotent")
public class IdempotentController {

    /**
     * 基本幂等 — Header 方式传 Token（默认）
     * <p>
     * 请求头携带 X-Idempotent-Token，首次消费成功，重复请求被拒绝
     */
    @Idempotent("order-submit")
    @PostMapping("/header")
    public CommonResult<String> headerToken(@RequestBody Map<String, String> body) {
        return CommonResult.success("订单提交成功，参数：" + body);
    }

    /**
     * Param 方式传 Token — from=PARAM，通过请求参数传递 Token
     */
    @Idempotent(value = "pay-confirm", from = IdempotentTokenFrom.PARAM, tokenName = "token")
    @PostMapping("/param")
    public CommonResult<String> paramToken(@RequestParam String token, @RequestBody Map<String, String> body) {
        return CommonResult.success("支付确认成功，参数：" + body);
    }

    /**
     * 自定义 tokenName — Header 名改为 X-Pay-Token
     */
    @Idempotent(value = "custom-token", tokenName = "X-Pay-Token")
    @PostMapping("/custom-token-name")
    public CommonResult<String> customTokenName(@RequestBody Map<String, String> body) {
        return CommonResult.success("自定义 tokenName 接口调用成功，参数：" + body);
    }

    /**
     * 返回 null — 验证结果缓存对 null 返回值的处理
     */
    @Idempotent("null-return")
    @PostMapping("/null-return")
    public CommonResult<String> nullReturn() {
        return null;
    }

    /**
     * 自定义拒绝提示 — message 自定义
     */
    @Idempotent(value = "custom-msg", message = "该操作正在处理中，请勿重复点击")
    @PostMapping("/custom-message")
    public CommonResult<String> customMessage(@RequestBody Map<String, String> body) {
        return CommonResult.success("自定义提示接口调用成功，参数：" + body);
    }
}
