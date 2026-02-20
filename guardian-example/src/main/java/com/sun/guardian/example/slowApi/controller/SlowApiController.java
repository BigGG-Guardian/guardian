package com.sun.guardian.example.slowApi.controller;

import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.slow.api.core.annotation.SlowApiThreshold;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 慢接口检测示例接口
 *
 * @author scj
 * @since 2026-02-20
 */
@RestController
@RequestMapping("/slow-api")
public class SlowApiController {

    /**
     * 正常接口 — 立即返回，不触发慢接口检测
     */
    @GetMapping("/fast")
    public CommonResult<String> fast() {
        return CommonResult.success("快速响应，不会触发慢接口检测");
    }

    /**
     * 模拟慢接口 — sleep 指定时间，超过全局阈值（3000ms）触发检测
     */
    @GetMapping("/slow")
    public CommonResult<String> slow(@RequestParam(defaultValue = "4000") long sleepMs) throws InterruptedException {
        Thread.sleep(sleepMs);
        return CommonResult.success("接口耗时 " + sleepMs + "ms（全局阈值 3000ms）");
    }

    /**
     * 注解自定义阈值 — 阈值设为 1000ms，sleep 超过 1s 即触发
     */
    @SlowApiThreshold(1000)
    @GetMapping("/custom-threshold")
    public CommonResult<String> customThreshold(@RequestParam(defaultValue = "1500") long sleepMs) throws InterruptedException {
        Thread.sleep(sleepMs);
        return CommonResult.success("接口耗时 " + sleepMs + "ms（注解阈值 1000ms）");
    }

    /**
     * 白名单接口 — 配置在 exclude-urls 中，无论耗时多长都不触发检测
     */
    @GetMapping("/health")
    public CommonResult<String> health(@RequestParam(defaultValue = "5000") long sleepMs) throws InterruptedException {
        Thread.sleep(sleepMs);
        return CommonResult.success("白名单接口，耗时 " + sleepMs + "ms 但不触发检测");
    }

    /**
     * 刚好临界值 — sleep 恰好等于全局阈值（3000ms），验证等于阈值时是否触发
     */
    @GetMapping("/boundary")
    public CommonResult<String> boundary() throws InterruptedException {
        Thread.sleep(3000);
        return CommonResult.success("接口耗时恰好 3000ms，等于全局阈值");
    }
}
