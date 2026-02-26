package com.sun.guardian.trace.core.support.thread;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * Spring TaskDecorator 实现，配合 @Async 线程池自动传递 traceId
 * 使用方式：在 Spring 配置中将此 Decorator 设置到线程池
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 19:49
 */
public class TraceTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }

            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
