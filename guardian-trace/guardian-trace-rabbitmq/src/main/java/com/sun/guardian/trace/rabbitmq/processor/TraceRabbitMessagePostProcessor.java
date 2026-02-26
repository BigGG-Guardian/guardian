package com.sun.guardian.trace.rabbitmq.processor;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.core.filter.TraceIdFilter;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/**
 * RabbitMQ 发送端 —— 将当前 traceId 写入消息 Header
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 20:11
 */
public class TraceRabbitMessagePostProcessor implements MessagePostProcessor {
    private final TraceConfig traceConfig;

    public TraceRabbitMessagePostProcessor(TraceConfig traceConfig) {
        this.traceConfig = traceConfig;
    }

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        if (traceId != null) {
            message.getMessageProperties().setHeader(traceConfig.getHeaderName(), traceId);
        }
        return message;
    }
}
