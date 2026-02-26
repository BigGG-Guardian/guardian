package com.sun.guardian.trace.core.support.thread;

import org.slf4j.MDC;

import java.util.Map;

/**
 * 支持 traceId 跨线程传递的 Runnable 包装器
 * 在创建时捕获当前线程的 MDC 上下文，执行时恢复到子线程
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 19:44
 */
public class TraceRunnable implements Runnable {

    private final Runnable delegate;
    private final Map<String, String> contextMap;

    public TraceRunnable(Runnable delegate) {
        this.delegate = delegate;
        this.contextMap = MDC.getCopyOfContextMap();
    }

    @Override
    public void run() {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }

        try {
            delegate.run();
        } finally {
            MDC.clear();
        }
    }
}
