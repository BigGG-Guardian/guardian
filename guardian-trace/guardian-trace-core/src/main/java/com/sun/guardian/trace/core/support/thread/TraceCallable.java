package com.sun.guardian.trace.core.support.thread;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 支持 traceId 跨线程传递的 Callable 包装器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 19:47
 */
public class TraceCallable<V> implements Callable<V> {

    private final Callable<V> delegate;
    private final Map<String, String> contextMap;

    public TraceCallable(Callable<V> delegate) {
        this.delegate = delegate;
        this.contextMap = MDC.getCopyOfContextMap();
    }

    @Override
    public V call() throws Exception {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }

        try {
            return delegate.call();
        } finally {
            MDC.clear();
        }
    }
}
