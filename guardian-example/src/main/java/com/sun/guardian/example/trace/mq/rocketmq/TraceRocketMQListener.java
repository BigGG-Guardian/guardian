//package com.sun.guardian.example.trace.mq.rocketmq;
//
//import org.apache.rocketmq.common.message.MessageExt;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Component;
//
///**
// * RocketMQ 单条消费端，验证 AOP 切面自动提取 traceId 并写入 MDC
// *
// * @author scj
// * @since 2026-02-09
// */
//@Component
//@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
//@RocketMQMessageListener(topic = TraceRocketMQController.TOPIC, consumerGroup = "guardian-example-consumer")
//public class TraceRocketMQListener implements RocketMQListener<MessageExt> {
//
//    private static final Logger log = LoggerFactory.getLogger(TraceRocketMQListener.class);
//
//    @Override
//    public void onMessage(MessageExt message) {
//        String traceId = MDC.get("traceId");
//        String body = new String(message.getBody());
//        log.info("[RocketMQ-单条消费] 收到消息: {}，traceId={}", body, traceId);
//    }
//}
