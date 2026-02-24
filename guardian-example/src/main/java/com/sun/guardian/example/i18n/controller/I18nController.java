package com.sun.guardian.example.i18n.controller;

import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.idempotent.core.annotation.Idempotent;
import com.sun.guardian.rate.limit.core.annotation.RateLimit;
import com.sun.guardian.repeat.submit.core.annotation.RepeatSubmit;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息国际化示例接口
 * <p>
 * 测试方法：请求头添加 Accept-Language 切换语言
 * <ul>
 *     <li>中文：Accept-Language: zh-CN</li>
 *     <li>英文：Accept-Language: en</li>
 *     <li>不传：使用系统默认语言</li>
 * </ul>
 *
 * @author scj
 * @since 2026-02-24
 */
@RestController
@RequestMapping("/i18n")
public class I18nController {

    /**
     * 限流 i18n — message 使用 i18n Key，触发限流后根据 Accept-Language 返回对应语言
     */
    @RateLimit(qps = 1, message = "guardian.rate-limit.rejected")
    @GetMapping("/rate-limit")
    public CommonResult<String> rateLimitI18n() {
        return CommonResult.success("限流 i18n 接口调用成功");
    }

    /**
     * 防重 i18n — message 使用 i18n Key，触发防重后根据 Accept-Language 返回对应语言
     */
    @RepeatSubmit(interval = 10, message = "guardian.repeat-submit.rejected")
    @PostMapping("/repeat-submit")
    public CommonResult<String> repeatSubmitI18n(@RequestBody Map<String, String> body) {
        return CommonResult.success("防重 i18n 接口调用成功，参数：" + body);
    }

    /**
     * 幂等 i18n — message 使用 i18n Key，Token 无效/已消费时根据 Accept-Language 返回对应语言
     */
    @Idempotent(value = "i18n-order", message = "guardian.idempotent.rejected")
    @PostMapping("/idempotent")
    public CommonResult<String> idempotentI18n(@RequestBody Map<String, String> body) {
        return CommonResult.success("幂等 i18n 接口调用成功，参数：" + body);
    }

    /**
     * 幂等 i18n（缺少 Token）— 不传 Token 触发 missing-token-message 的国际化
     */
    @Idempotent(value = "i18n-missing-token")
    @PostMapping("/idempotent/missing-token")
    public CommonResult<String> idempotentMissingTokenI18n(@RequestBody Map<String, String> body) {
        return CommonResult.success("不应到达此处（缺少 Token 应被拦截）");
    }
}
