package com.sun.guardian.example.trace.controller;

import com.sun.guardian.example.common.CommonResult;
import com.sun.guardian.trace.core.support.utils.TraceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

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

    /**
     * 跨线程传递 — TraceUtils.wrap(Runnable)，子线程 traceId 与主线程一致
     */
    @GetMapping("/cross-thread")
    public CommonResult<Map<String, Object>> crossThread() throws Exception {
        String mainTraceId = MDC.get("traceId");
        log.info("[主线程] traceId={}", mainTraceId);

        List<String> childTraceIds = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);

        executor.submit(TraceUtils.wrap(() -> {
            String childTraceId = MDC.get("traceId");
            log.info("[子线程-Runnable] traceId={}", childTraceId);
            synchronized (childTraceIds) {
                childTraceIds.add(childTraceId);
            }
            latch.countDown();
        }));

        Future<String> future = executor.submit(TraceUtils.wrap(() -> {
            String childTraceId = MDC.get("traceId");
            log.info("[子线程-Callable] traceId={}", childTraceId);
            latch.countDown();
            return childTraceId;
        }));

        latch.await(5, TimeUnit.SECONDS);
        childTraceIds.add(future.get());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mainTraceId", mainTraceId);
        result.put("childTraceIds", childTraceIds);
        result.put("allMatch", childTraceIds.stream().allMatch(id -> id != null && id.equals(mainTraceId)));
        result.put("description", "查看控制台日志，主线程和子线程的 traceId 应完全一致");
        return CommonResult.success(result);
    }

    /**
     * 跨线程传递 — TraceUtils.wrap(Executor) 包装线程池，配合 CompletableFuture
     */
    @GetMapping("/completable-future")
    public CommonResult<Map<String, Object>> completableFuture() throws Exception {
        String mainTraceId = MDC.get("traceId");
        log.info("[主线程] traceId={}", mainTraceId);

        java.util.concurrent.Executor tracedExecutor = TraceUtils.wrap(executor);

        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            String childTraceId = MDC.get("traceId");
            log.info("[CompletableFuture-1] traceId={}", childTraceId);
            return childTraceId;
        }, tracedExecutor);

        CompletableFuture<String> f2 = f1.thenApplyAsync(prev -> {
            String childTraceId = MDC.get("traceId");
            log.info("[CompletableFuture-2] traceId={}", childTraceId);
            return childTraceId;
        }, tracedExecutor);

        String task1TraceId = f1.get(5, TimeUnit.SECONDS);
        String task2TraceId = f2.get(5, TimeUnit.SECONDS);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mainTraceId", mainTraceId);
        result.put("task1TraceId", task1TraceId);
        result.put("task2TraceId", task2TraceId);
        result.put("allMatch", mainTraceId.equals(task1TraceId) && mainTraceId.equals(task2TraceId));
        result.put("description", "CompletableFuture 链式调用，所有异步任务的 traceId 应与主线程一致");
        return CommonResult.success(result);
    }

    /**
     * TraceUtils.getTraceId() — 工具类获取当前 traceId
     */
    @GetMapping("/get-trace-id")
    public CommonResult<Map<String, String>> getTraceId() {
        String traceIdFromMdc = MDC.get("traceId");
        String traceIdFromUtils = TraceUtils.getTraceId();
        log.info("TraceUtils.getTraceId()={}", traceIdFromUtils);
        Map<String, String> result = new LinkedHashMap<>();
        result.put("traceIdFromMdc", traceIdFromMdc);
        result.put("traceIdFromUtils", traceIdFromUtils);
        result.put("match", String.valueOf(traceIdFromMdc != null && traceIdFromMdc.equals(traceIdFromUtils)));
        result.put("description", "TraceUtils.getTraceId() 与 MDC.get(\"traceId\") 返回值一致");
        return CommonResult.success(result);
    }

    /**
     * TraceUtils.switchTraceId() — 模拟 MQ 批量消费场景逐条切换 traceId
     */
    @GetMapping("/switch-trace-id")
    public CommonResult<Map<String, Object>> switchTraceId() {
        String originalTraceId = MDC.get("traceId");
        log.info("[原始] traceId={}", originalTraceId);

        List<Map<String, String>> records = new ArrayList<>();
        String[] simulatedMsgTraceIds = {"msg-trace-001", "msg-trace-002", "msg-trace-003"};

        for (String msgTraceId : simulatedMsgTraceIds) {
            TraceUtils.switchTraceId(msgTraceId);
            log.info("[消息处理] 当前 traceId={}", MDC.get("traceId"));
            Map<String, String> record = new LinkedHashMap<>();
            record.put("msgTraceId", msgTraceId);
            record.put("currentMdc", MDC.get("traceId"));
            records.add(record);
        }

        TraceUtils.switchTraceId(originalTraceId);
        log.info("[恢复] traceId={}", MDC.get("traceId"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("originalTraceId", originalTraceId);
        result.put("switchRecords", records);
        result.put("restoredTraceId", MDC.get("traceId"));
        result.put("description", "模拟 MQ 批量消费，逐条切换 traceId 后恢复原始值");
        return CommonResult.success(result);
    }

    private void doService(String traceId) {
        log.info("[Service] 执行业务逻辑，traceId={}", traceId);
    }

    private void doDao(String traceId) {
        log.info("[Dao] 执行数据库操作，traceId={}", traceId);
    }
}
