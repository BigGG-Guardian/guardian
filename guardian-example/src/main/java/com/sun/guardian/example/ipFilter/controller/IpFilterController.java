package com.sun.guardian.example.ipFilter.controller;

import com.sun.guardian.example.common.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IP 黑白名单示例接口
 *
 * @author scj
 * @since 2026-02-24
 */
@RestController
@RequestMapping("/ip-filter")
public class IpFilterController {

    /**
     * 普通接口 — 不在黑名单且不受白名单约束时可正常访问
     */
    @GetMapping("/open")
    public CommonResult<String> open() {
        return CommonResult.success("此接口不受白名单限制，黑名单外均可访问");
    }

    /**
     * 白名单接口 — 仅配置的白名单 IP 可访问，其余 IP 拒绝
     */
    @GetMapping("/admin/dashboard")
    public CommonResult<String> adminDashboard() {
        return CommonResult.success("白名单接口访问成功");
    }

    /**
     * 白名单接口 — 模拟内部管理系统入口
     */
    @GetMapping("/admin/settings")
    public CommonResult<String> adminSettings() {
        return CommonResult.success("管理后台设置页访问成功");
    }

    /**
     * 黑名单测试 — 将本机 IP 加入黑名单后该接口被拦截
     */
    @GetMapping("/test-black")
    public CommonResult<String> testBlack() {
        return CommonResult.success("未被黑名单拦截，访问成功");
    }
}
