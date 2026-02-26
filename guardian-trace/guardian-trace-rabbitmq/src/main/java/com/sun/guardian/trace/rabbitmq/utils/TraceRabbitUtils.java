package com.sun.guardian.trace.rabbitmq.utils;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.core.support.utils.TraceUtils;
import org.springframework.amqp.core.Message;

/**
 * RabbitMQ 批量消费场景 traceId 切换工具
 * <p>
 * 在批量消费循环中逐条调用 {@link #switchTraceId(Message)}，自动从消息 Header 提取并切换 traceId：
 * <pre>
 * {@code @RabbitListener}(queues = "xxx", containerFactory = "batchContainerFactory")
 * public void onBatch(List<Message> messages) {
 *     for (Message msg : messages) {
 *         TraceRabbitUtils.switchTraceId(msg);
 *         // 业务逻辑...
 *     }
 * }
 * </pre>
 *
 * @author scj
 * @since 2026-02-09
 */
public final class TraceRabbitUtils {

    private static volatile TraceConfig traceConfig;

    private TraceRabbitUtils() {
    }

    public static void setTraceConfig(TraceConfig config) {
        traceConfig = config;
    }

    /**
     * 从 RabbitMQ {@link Message} Header 中提取 traceId 并切换到当前线程 MDC
     *
     * @param message RabbitMQ 消息
     */
    public static void switchTraceId(Message message) {
        if (traceConfig == null || message == null) {
            return;
        }
        Object value = message.getMessageProperties().getHeader(traceConfig.getHeaderName());
        TraceUtils.switchTraceId(value != null ? value.toString() : null);
    }
}
