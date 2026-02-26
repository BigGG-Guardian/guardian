//package com.sun.guardian.example.trace.mq.rabbitmq;
//
//import com.sun.guardian.example.common.CommonResult;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
///**
// * RabbitMQ 链路追踪测试接口
// *
// * @author scj
// * @since 2026-02-09
// */
//@RestController
//@RequestMapping("/trace/mq/rabbitmq")
//public class TraceRabbitController {
//
//    private static final Logger log = LoggerFactory.getLogger(TraceRabbitController.class);
//
//    public static final String EXCHANGE = "guardian.trace.test";
//    public static final String ROUTING_KEY = "trace.test";
//    public static final String QUEUE = "guardian.trace.test.queue";
//    public static final String BATCH_ROUTING_KEY = "trace.test.batch";
//    public static final String BATCH_QUEUE = "guardian.trace.test.batch.queue";
//
//    private final RabbitTemplate rabbitTemplate;
//
//    public TraceRabbitController(RabbitTemplate rabbitTemplate) {
//        this.rabbitTemplate = rabbitTemplate;
//    }
//
//    /**
//     * 发送单条 RabbitMQ 消息，验证 traceId 自动注入消息 Header
//     */
//    @GetMapping("/send")
//    public CommonResult<Map<String, String>> send(@RequestParam(defaultValue = "Hello RabbitMQ") String message) {
//        String traceId = MDC.get("traceId");
//        log.info("[RabbitMQ-发送端] 发送消息: {}，traceId={}", message, traceId);
//        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
//
//        Map<String, String> result = new LinkedHashMap<>();
//        result.put("traceId", traceId);
//        result.put("message", message);
//        result.put("description", "消息已发送，查看消费端日志验证 traceId 一致");
//        return CommonResult.success(result);
//    }
//
//    /**
//     * 批量发送 RabbitMQ 消息，验证批量消费场景 traceId 逐条切换
//     */
//    @GetMapping("/send-batch")
//    public CommonResult<Map<String, Object>> sendBatch(@RequestParam(defaultValue = "3") int count) {
//        String traceId = MDC.get("traceId");
//        log.info("[RabbitMQ-批量发送] 发送 {} 条消息，traceId={}", count, traceId);
//        for (int i = 1; i <= count; i++) {
//            rabbitTemplate.convertAndSend(EXCHANGE, BATCH_ROUTING_KEY, "batch-msg-" + i);
//        }
//
//        Map<String, Object> result = new LinkedHashMap<>();
//        result.put("traceId", traceId);
//        result.put("count", count);
//        result.put("description", "已发送 " + count + " 条消息到批量队列，查看消费端日志验证 switchTraceId 逐条切换");
//        return CommonResult.success(result);
//    }
//}
