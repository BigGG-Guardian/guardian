//package com.sun.guardian.example.trace.mq.rocketmq;
//
//import org.apache.rocketmq.common.message.MessageExt;
//import org.apache.rocketmq.spring.annotation.ConsumeMode;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Component;
//
///**
// * RocketMQ 批量消费端（逐条回调），验证 AOP 切面自动注入 traceId
// * <p>
// * RocketMQ Spring Boot Starter 的 RocketMQListener 本身是逐条回调，
// * AOP 切面会自动为每次调用注入/清理 traceId，无需手动切换。
// *
// * @author scj
// * @since 2026-02-09
// */
//@Component
//@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
//@RocketMQMessageListener(
//        topic = TraceRocketMQController.BATCH_TOPIC,
//        consumerGroup = "guardian-example-batch-consumer",
//        consumeMode = ConsumeMode.CONCURRENTLY
//)
//public class TraceRocketMQBatchListener implements RocketMQListener<MessageExt> {
//
//    private static final Logger log = LoggerFactory.getLogger(TraceRocketMQBatchListener.class);
//
//    @Override
//    public void onMessage(MessageExt message) {
//        String traceId = MDC.get("traceId");
//        String body = new String(message.getBody());
//        log.info("[RocketMQ-批量消费] 消息: {}，traceId={}", body, traceId);
//    }
//}
