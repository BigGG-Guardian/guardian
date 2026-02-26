package com.sun.guardian.trace.core.support.utils;

import com.sun.guardian.trace.core.filter.TraceIdFilter;
import com.sun.guardian.trace.core.support.thread.TraceCallable;
import com.sun.guardian.trace.core.support.thread.TraceRunnable;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * Trace 工具类，提供跨线程传递 traceId 的快捷方法
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 19:51
 */
public class TraceUtils {
    public TraceUtils() {
    }

    /**
     * 包装 Runnable，使其在子线程中携带当前 traceId
     */
    public static Runnable wrap(Runnable runnable) {
        return new TraceRunnable(runnable);
    }

    /**
     * 包装 Callable，使其在子线程中携带当前 traceId
     */
    public static <V> Callable<V> wrap(Callable<V> callable) {
        return new TraceCallable<>(callable);
    }

    /**
     * 包装 Executor，使其提交的所有任务自动携带 traceId
     */
    public static Executor wrap(Executor executor) {
        return command -> {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            executor.execute(() -> {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }

                try {
                    command.run();
                } finally {
                    MDC.clear();
                }
            });
        };
    }

    /**
     * 获取当前线程的 traceId
     */
    public static String getTraceId() {
        return MDC.get(TraceIdFilter.MDC_KEY);
    }

    /**
     * 切换当前线程的 traceId，用于 MQ 批量消费场景逐条切换
     *
     * @param traceId 目标 traceId，为 null 时仅清除当前 traceId
     */
    public static void switchTraceId(String traceId) {
        MDC.remove(TraceIdFilter.MDC_KEY);
        if (traceId != null) {
            MDC.put(TraceIdFilter.MDC_KEY, traceId);
        }
    }
}
