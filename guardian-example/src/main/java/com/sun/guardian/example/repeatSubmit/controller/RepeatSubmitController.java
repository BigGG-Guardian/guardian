package com.sun.guardian.example.repeatSubmit.controller;

import cn.hutool.core.util.StrUtil;
import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.repeat.submit.core.annotation.RepeatSubmit;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 防重复提交示例接口
 * <p>
 * 演示 Guardian 的多种使用方式：
 * <ul>
 *   <li>YAML URL 规则匹配（GET / POST）</li>
 *   <li>{@link RepeatSubmit @RepeatSubmit} 注解方式</li>
 *   <li>排除规则（白名单）</li>
 * </ul>
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 10:53
 */
@RestController
@RequestMapping("")
public class RepeatSubmitController {

    /**
     * GET 请求 — YAML 规则防重
     * <p>
     * 当前 application.yml 中配置了排除规则 {@code /guardian-example/repeat-submit/yml/getLimit/1}，
     * 因此此接口实际会被白名单放行，不受防重限制。可用于验证排除规则是否生效。
     *
     * @param params 请求参数
     * @return 成功结果
     */
    @GetMapping("/repeat-submit/yml/getLimit/1")
    public CommonResult<Boolean> getLimit1(@RequestParam Map<String, String> params) {
        System.out.println(StrUtil.format("/getLimit/1 接口调用成功，请求参数：{}", params.toString()));
        return CommonResult.success(true);
    }

    /**
     * POST 请求 — YAML 规则防重
     * <p>
     * 由 YAML 配置 {@code /guardian-example/repeat-submit/yml/**} 规则拦截，interval=50s。
     * 相同参数在 50 秒内重复提交会被拦截。
     *
     * @param body 请求体
     * @return 成功结果
     */
    @PostMapping("/repeat-submit/yml/postLimit/1")
    public CommonResult<Boolean> postLimit1(@RequestBody Map<String, String> body) {
        System.out.println(StrUtil.format("/postLimit/1 接口调用成功，请求参数：{}", body.toString()));
        return CommonResult.success(true);
    }

    /**
     * POST 请求 — 注解方式防重
     * <p>
     * 未被 YAML 规则命中时，由 {@link RepeatSubmit @RepeatSubmit} 注解兜底。
     * 演示注解方式的防重配置。
     *
     * @param body 请求体
     * @return 成功结果
     */
    @RepeatSubmit(interval = 10, message = "请勿重复提交")
    @PostMapping("/repeat-submit/annotation/postLimit/2")
    public CommonResult<Boolean> postLimit2(@RequestBody Map<String, String> body) {
        System.out.println(StrUtil.format("/postLimit/2 接口调用成功，请求参数：{}", body.toString()));
        return CommonResult.success(true);
    }

    /**
     * POST 请求 — 注解方式防重,主动抛异常释放锁
     * <p>
     * 未被 YAML 规则命中时，由 {@link RepeatSubmit @RepeatSubmit} 注解兜底。
     * 演示主动抛异常释放锁。
     *
     * @param body 请求体
     * @return 成功结果
     */
    @RepeatSubmit(interval = 10, message = "请勿重复提交")
    @PostMapping("/repeat-submit/annotation/postLimit/3")
    public CommonResult<Boolean> postLimit3(@RequestBody Map<String, String> body) {
        System.out.println(StrUtil.format("/postLimit/2 接口调用成功，请求参数：{}", body.toString()));
        throw new RuntimeException("测试释放锁");
    }
}
