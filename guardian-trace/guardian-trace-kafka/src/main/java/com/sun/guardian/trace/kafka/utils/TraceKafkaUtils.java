package com.sun.guardian.trace.kafka.utils;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.core.support.utils.TraceUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.nio.charset.StandardCharsets;

/**
 * Kafka 批量消费场景 traceId 切换工具
 * <p>
 * 在批量消费循环中逐条调用 {@link #switchTraceId(ConsumerRecord)}，自动从消息 Header 提取并切换 traceId：
 * <pre>
 * {@code @KafkaListener}(topics = "xxx", batch = "true")
 * public void onBatch(List&lt;ConsumerRecord&lt;String, String&gt;&gt; records) {
 *     for (ConsumerRecord&lt;String, String&gt; record : records) {
 *         TraceKafkaUtils.switchTraceId(record);
 *         // 业务逻辑...
 *     }
 * }
 * </pre>
 *
 * @author scj
 * @since 2026-02-09
 */
public final class TraceKafkaUtils {

    private static volatile TraceConfig traceConfig;

    private TraceKafkaUtils() {
    }

    public static void setTraceConfig(TraceConfig config) {
        traceConfig = config;
    }

    /**
     * 从 Kafka {@link ConsumerRecord} Header 中提取 traceId 并切换到当前线程 MDC
     *
     * @param record Kafka 消费记录
     */
    public static void switchTraceId(ConsumerRecord<?, ?> record) {
        if (traceConfig == null || record == null) {
            return;
        }
        Header header = record.headers().lastHeader(traceConfig.getHeaderName());
        TraceUtils.switchTraceId(header != null ? new String(header.value(), StandardCharsets.UTF_8) : null);
    }
}
