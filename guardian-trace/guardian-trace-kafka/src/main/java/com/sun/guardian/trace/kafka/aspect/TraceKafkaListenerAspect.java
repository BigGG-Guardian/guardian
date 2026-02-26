package com.sun.guardian.trace.kafka.aspect;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.core.filter.TraceIdFilter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Kafka 消费端 AOP 切面，拦截 @KafkaListener / @KafkaHandler 方法自动注入/清理 traceId
 * <p>
 * 不占用 RecordInterceptor / BatchInterceptor，接入方可自由注册自己的拦截器。
 * 批量消费场景取第一条消息的 traceId，如需逐条切换请在循环中调用 {@code TraceUtils.switchTraceId()}
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 21:25
 */
@Aspect
public class TraceKafkaListenerAspect {

    private final TraceConfig traceConfig;

    public TraceKafkaListenerAspect(TraceConfig traceConfig) {
        this.traceConfig = traceConfig;
    }

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener) || "
            + "@annotation(org.springframework.kafka.annotation.KafkaHandler)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        String traceId = extractTraceId(pjp.getArgs());

        if (traceId != null) {
            MDC.put(TraceIdFilter.MDC_KEY, traceId);
        }

        try {
            return pjp.proceed();
        } finally {
            MDC.remove(TraceIdFilter.MDC_KEY);
        }
    }

    private String extractTraceId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof ConsumerRecord) {
                String traceId = extractFromRecord((ConsumerRecord<?, ?>) arg);
                if (traceId != null) {
                    return traceId;
                }
            }
            if (arg instanceof List) {
                List<?> list = (List<?>) arg;
                if (!list.isEmpty() && list.get(0) instanceof ConsumerRecord) {
                    return extractFromRecord((ConsumerRecord<?, ?>) list.get(0));
                }
            }
        }
        return null;
    }

    private String extractFromRecord(ConsumerRecord<?, ?> record) {
        Header header = record.headers().lastHeader(traceConfig.getHeaderName());
        if (header != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return null;
    }
}
