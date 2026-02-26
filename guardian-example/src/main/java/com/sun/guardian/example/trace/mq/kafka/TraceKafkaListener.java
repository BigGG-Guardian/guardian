//package com.sun.guardian.example.trace.mq.kafka;
//
//import com.sun.guardian.trace.kafka.utils.TraceKafkaUtils;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * Kafka 消费端，验证 AOP 切面自动提取 traceId 并写入 MDC
// *
// * @author scj
// * @since 2026-02-09
// */
//@Component
//@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
//public class TraceKafkaListener {
//
//    private static final Logger log = LoggerFactory.getLogger(TraceKafkaListener.class);
//
//    /**
//     * 单条消费 — 参数必须是 ConsumerRecord 类型，AOP 切面才能从 Header 中提取 traceId
//     */
//    @KafkaListener(topics = TraceKafkaController.TOPIC, groupId = "guardian-example")
//    public void onMessage(ConsumerRecord<String, String> record) {
//        String traceId = MDC.get("traceId");
//        log.info("[Kafka-单条消费] 收到消息: {}，traceId={}", record.value(), traceId);
//    }
//
//    /**
//     * 批量消费 — AOP 切面自动设置第一条消息的 traceId，循环中用工具类逐条切换
//     */
//    @KafkaListener(topics = TraceKafkaController.BATCH_TOPIC, groupId = "guardian-example-batch", batch = "true")
//    public void onBatchMessage(List<ConsumerRecord<String, String>> records) {
//        log.info("[Kafka-批量消费] 收到 {} 条消息，首条 traceId={}", records.size(), MDC.get("traceId"));
//        for (ConsumerRecord<String, String> record : records) {
//            TraceKafkaUtils.switchTraceId(record);
//            log.info("[Kafka-批量消费-逐条] 消息: {}，traceId={}", record.value(), MDC.get("traceId"));
//        }
//    }
//}
