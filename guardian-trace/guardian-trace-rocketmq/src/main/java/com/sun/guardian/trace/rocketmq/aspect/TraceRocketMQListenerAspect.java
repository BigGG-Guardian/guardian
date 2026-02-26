package com.sun.guardian.trace.rocketmq.aspect;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.core.filter.TraceIdFilter;
import org.apache.rocketmq.common.message.MessageExt;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;

import java.util.List;

/**
 * RocketMQ 消费端 AOP 切面，拦截 @RocketMQMessageListener 注解的类中的方法自动注入/清理 traceId
 * <p>
 * 不占用 ConsumeMessageHook，接入方可自由注册自己的 Hook。
 * 批量消费场景取第一条消息的 traceId，如需逐条切换请在循环中调用 {@code TraceUtils.switchTraceId()}
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 21:32
 */
@Aspect
public class TraceRocketMQListenerAspect {

    private final TraceConfig traceConfig;

    public TraceRocketMQListenerAspect(TraceConfig traceConfig) {
        this.traceConfig = traceConfig;
    }

    @Around("@within(org.apache.rocketmq.spring.annotation.RocketMQMessageListener)")
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
            if (arg instanceof MessageExt) {
                String traceId = ((MessageExt) arg).getUserProperty(traceConfig.getHeaderName());
                if (traceId != null) {
                    return traceId;
                }
            }
            if (arg instanceof List) {
                List<?> list = (List<?>) arg;
                if (!list.isEmpty() && list.get(0) instanceof MessageExt) {
                    return ((MessageExt) list.get(0)).getUserProperty(traceConfig.getHeaderName());
                }
            }
        }
        return null;
    }
}
