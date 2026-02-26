package com.sun.guardian.trace.kafka.interceptor;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.core.filter.TraceIdFilter;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Kafka 发送端拦截器，将当前线程的 traceId 写入消息 Header
 * <p>
 * 静态持有 {@link TraceConfig} 引用，每次发送时动态获取 headerName，支持配置中心热更新。
 * 接入方需在 Kafka Producer 配置中注册此拦截器：
 * <pre>
 * spring.kafka.producer.properties.interceptor.classes=\
 *   com.sun.guardian.trace.kafka.interceptor.TraceKafkaProducerInterceptor
 * </pre>
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 20:33
 */
public class TraceKafkaProducerInterceptor implements ProducerInterceptor<Object, Object> {

    private static volatile TraceConfig traceConfig;

    /**
     * 由自动配置类在 @PostConstruct 阶段调用，注入 TraceConfig 实例
     *
     * @param config TraceConfig 配置接口
     */
    public static void setTraceConfig(TraceConfig config) {
        traceConfig = config;
    }

    @Override
    public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> producerRecord) {
        if (traceConfig == null) {
            return producerRecord;
        }
        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        if (traceId != null) {
            producerRecord.headers().add(traceConfig.getHeaderName(), traceId.getBytes(StandardCharsets.UTF_8));
        }
        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> map) {
    }
}
