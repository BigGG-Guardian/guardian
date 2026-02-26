//package com.sun.guardian.example.trace.mq.rabbitmq;
//
//import com.sun.guardian.trace.rabbitmq.utils.TraceRabbitUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * RabbitMQ 消费端，验证 AOP 切面自动提取 traceId 并写入 MDC
// *
// * @author scj
// * @since 2026-02-09
// */
//@Component
//public class TraceRabbitListener {
//
//    private static final Logger log = LoggerFactory.getLogger(TraceRabbitListener.class);
//
//    /**
//     * 单条消费 — 参数必须是 Message 类型，AOP 切面才能从 Header 中提取 traceId
//     */
//    @RabbitListener(queues = TraceRabbitController.QUEUE)
//    public void onMessage(Message message) {
//        String traceId = MDC.get("traceId");
//        String body = new String(message.getBody());
//        log.info("[RabbitMQ-单条消费] 收到消息: {}，traceId={}", body, traceId);
//    }
//
//    /**
//     * 批量消费 — AOP 切面自动设置第一条消息的 traceId，循环中用工具类逐条切换
//     */
//    @RabbitListener(queues = TraceRabbitController.BATCH_QUEUE, containerFactory = "batchContainerFactory")
//    public void onBatchMessage(List<Message> messages) {
//        log.info("[RabbitMQ-批量消费] 收到 {} 条消息，首条 traceId={}", messages.size(), MDC.get("traceId"));
//        for (Message msg : messages) {
//            TraceRabbitUtils.switchTraceId(msg);
//            String body = new String(msg.getBody());
//            log.info("[RabbitMQ-批量消费-逐条] 消息: {}，traceId={}", body, MDC.get("traceId"));
//        }
//    }
//}
