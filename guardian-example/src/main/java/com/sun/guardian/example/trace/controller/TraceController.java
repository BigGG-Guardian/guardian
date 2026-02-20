package com.sun.guardian.example.trace.controller;

import com.sun.guardian.example.common.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求链路 TraceId 示例接口
 *
 * @author scj
 * @since 2026-02-20
 */
@RestController
@RequestMapping("/trace")
public class TraceController {

    private static final Logger log = LoggerFactory.getLogger(TraceController.class);

    /**
     * 基本 TraceId — 查看响应头 X-Trace-Id 和日志中的 traceId
     */
    @GetMapping("/basic")
    public CommonResult<Map<String, String>> basic() {
        String traceId = MDC.get("traceId");
        log.info("基本 TraceId 接口被调用，当前 traceId={}", traceId);
        Map<String, String> result = new LinkedHashMap<>();
        result.put("traceId", traceId);
        result.put("description", "查看响应头 X-Trace-Id 和控制台日志");
        return CommonResult.success(result);
    }

    /**
     * 模拟多层调用 — 同一请求内多次打日志，验证 traceId 一致
     */
    @GetMapping("/multi-log")
    public CommonResult<Map<String, String>> multiLog() {
        String traceId = MDC.get("traceId");
        log.info("[Controller] 接收请求，traceId={}", traceId);
        doService(traceId);
        doDao(traceId);
        log.info("[Controller] 请求处理完成");
        Map<String, String> result = new LinkedHashMap<>();
        result.put("traceId", traceId);
        result.put("description", "查看控制台，3条日志都带相同的 traceId");
        return CommonResult.success(result);
    }

    /**
     * 透传 TraceId — 请求头携带 X-Trace-Id，验证 Filter 复用而非重新生成
     */
    @GetMapping("/passthrough")
    public CommonResult<Map<String, String>> passthrough() {
        String traceId = MDC.get("traceId");
        log.info("透传 TraceId 接口被调用，traceId={}", traceId);
        Map<String, String> result = new LinkedHashMap<>();
        result.put("traceId", traceId);
        result.put("description", "请求头传入 X-Trace-Id: my-custom-trace-123，响应头和日志应返回同一个值");
        return CommonResult.success(result);
    }

    private void doService(String traceId) {
        log.info("[Service] 执行业务逻辑，traceId={}", traceId);
    }

    private void doDao(String traceId) {
        log.info("[Dao] 执行数据库操作，traceId={}", traceId);
    }
}
