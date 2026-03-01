package com.sun.guardian.example.apiSwitch.controller;

import com.sun.guardian.example.common.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口开关示例接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-28 22:29
 */
@RestController
@RequestMapping("/api-switch")
public class ApiSwitchController {

    /**
     * 正常接口 — 立即返回，不触发接口关闭
     */
    @GetMapping("/un-disabled")
    public CommonResult<String> unDisabled() {
        return CommonResult.success("正常响应，不触发接口开关检测");
    }

    /**
     * yml关闭接口 — 通过yml配置关闭接口
     */
    @GetMapping("/disabled")
    public CommonResult<String> disabled() {
        return CommonResult.success("yml关闭接口，触发接口开关检测");
    }

    /**
     * actuator关闭接口 — 通过actuator配置关闭接口
     */
    @GetMapping("/actuator-disabled")
    public CommonResult<String> actuatorDisabled() {
        return CommonResult.success("actuator关闭接口，触发接口开关检测");
    }

}
