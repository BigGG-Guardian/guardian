//package com.sun.guardian.example.trace.mq.rocketmq;
//
//import com.sun.guardian.example.common.CommonResult;
//import org.apache.rocketmq.spring.core.RocketMQTemplate;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
///**
// * RocketMQ 链路追踪测试接口
// *
// * @author scj
// * @since 2026-02-09
// */
//@RestController
//@RequestMapping("/trace/mq/rocketmq")
//@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
//public class TraceRocketMQController {
//
//    private static final Logger log = LoggerFactory.getLogger(TraceRocketMQController.class);
//
//    public static final String TOPIC = "guardian-trace-test";
//    public static final String BATCH_TOPIC = "guardian-trace-test-batch";
//
//    private final RocketMQTemplate rocketMQTemplate;
//
//    public TraceRocketMQController(RocketMQTemplate rocketMQTemplate) {
//        this.rocketMQTemplate = rocketMQTemplate;
//    }
//
//    /**
//     * 发送单条 RocketMQ 消息
//     */
//    @GetMapping("/send")
//    public CommonResult<Map<String, String>> send(@RequestParam(defaultValue = "Hello RocketMQ") String message) {
//        String traceId = MDC.get("traceId");
//        log.info("[RocketMQ-发送端] 发送消息: {}，traceId={}", message, traceId);
//        rocketMQTemplate.convertAndSend(TOPIC, message);
//
//        Map<String, String> result = new LinkedHashMap<>();
//        result.put("traceId", traceId);
//        result.put("message", message);
//        result.put("description", "消息已发送，查看消费端日志验证 traceId 一致");
//        return CommonResult.success(result);
//    }
//
//    /**
//     * 批量发送 RocketMQ 消息
//     */
//    @GetMapping("/send-batch")
//    public CommonResult<Map<String, Object>> sendBatch(@RequestParam(defaultValue = "3") int count) {
//        String traceId = MDC.get("traceId");
//        log.info("[RocketMQ-批量发送] 发送 {} 条消息，traceId={}", count, traceId);
//        for (int i = 1; i <= count; i++) {
//            rocketMQTemplate.convertAndSend(BATCH_TOPIC, "batch-msg-" + i);
//        }
//
//        Map<String, Object> result = new LinkedHashMap<>();
//        result.put("traceId", traceId);
//        result.put("count", count);
//        result.put("description", "已发送 " + count + " 条消息到批量 topic，查看消费端日志验证 switchTraceId 逐条切换");
//        return CommonResult.success(result);
//    }
//}
