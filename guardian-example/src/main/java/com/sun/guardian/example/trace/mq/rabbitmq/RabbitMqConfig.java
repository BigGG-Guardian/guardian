//package com.sun.guardian.example.trace.mq.rabbitmq;
//
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.DirectExchange;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * RabbitMQ 队列/交换机/绑定声明 + 批量消费容器工厂
// *
// * @author scj
// * @since 2026-02-09
// */
//@Configuration
//public class RabbitMqConfig {
//
//    @Bean
//    public DirectExchange traceTestExchange() {
//        return new DirectExchange(TraceRabbitController.EXCHANGE);
//    }
//
//    @Bean
//    public Queue traceTestQueue() {
//        return new Queue(TraceRabbitController.QUEUE, true);
//    }
//
//    @Bean
//    public Binding traceTestBinding(Queue traceTestQueue, DirectExchange traceTestExchange) {
//        return BindingBuilder.bind(traceTestQueue).to(traceTestExchange).with(TraceRabbitController.ROUTING_KEY);
//    }
//
//    @Bean
//    public Queue traceTestBatchQueue() {
//        return new Queue(TraceRabbitController.BATCH_QUEUE, true);
//    }
//
//    @Bean
//    public Binding traceTestBatchBinding(Queue traceTestBatchQueue, DirectExchange traceTestExchange) {
//        return BindingBuilder.bind(traceTestBatchQueue).to(traceTestExchange).with(TraceRabbitController.BATCH_ROUTING_KEY);
//    }
//
//    /**
//     * 批量消费容器工厂，每批最多接收 10 条，等待 3 秒凑批
//     */
//    @Bean
//    public SimpleRabbitListenerContainerFactory batchContainerFactory(ConnectionFactory connectionFactory) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setConsumerBatchEnabled(true);
//        factory.setBatchSize(10);
//        factory.setBatchListener(true);
//        factory.setReceiveTimeout(3000L);
//        return factory;
//    }
//}
