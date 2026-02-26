package com.sun.guardian.trace.rabbitmq.aspect;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.core.filter.TraceIdFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;

import java.util.List;

/**
 * RabbitMQ 消费端 AOP 切面，拦截 @RabbitListener / @RabbitHandler 方法自动注入/清理 traceId
 * <p>
 * 不占用 AdviceChain，接入方可自由注册自己的拦截器。
 * 批量消费场景取第一条消息的 traceId，如需逐条切换请在循环中调用 {@code TraceUtils.switchTraceId()}
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 20:13
 */
@Aspect
public class TraceRabbitListenerAspect {

    private final TraceConfig traceConfig;

    public TraceRabbitListenerAspect(TraceConfig traceConfig) {
        this.traceConfig = traceConfig;
    }

    @Around("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener) || "
            + "@annotation(org.springframework.amqp.rabbit.annotation.RabbitHandler)")
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
            if (arg instanceof Message) {
                String traceId = extractFromMessage((Message) arg);
                if (traceId != null) {
                    return traceId;
                }
            }
            if (arg instanceof List) {
                List<?> list = (List<?>) arg;
                if (!list.isEmpty() && list.get(0) instanceof Message) {
                    return extractFromMessage((Message) list.get(0));
                }
            }
        }
        return null;
    }

    private String extractFromMessage(Message message) {
        Object value = message.getMessageProperties().getHeader(traceConfig.getHeaderName());
        return value != null ? value.toString() : null;
    }
}
