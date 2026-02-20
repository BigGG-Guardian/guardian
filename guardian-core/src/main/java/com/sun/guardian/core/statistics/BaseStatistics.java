package com.sun.guardian.core.statistics;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 拦截统计基类（内存，JVM 重启清零）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09
 */
public class BaseStatistics {

    private final AtomicLong totalBlockCount = new AtomicLong(0);
    private final AtomicLong totalPassCount = new AtomicLong(0);
    protected final ConcurrentHashMap<String, AtomicLong> uriBlockCount = new ConcurrentHashMap<>();

    /**
     * 记录一次拦截
     */
    public void recordBlock(String uri) {
        totalBlockCount.incrementAndGet();
        uriBlockCount.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 记录一次放行
     */
    public void recordPass() {
        totalPassCount.incrementAndGet();
    }

    /**
     * 总拦截次数
     */
    public long getTotalBlockCount() {
        return totalBlockCount.get();
    }

    /**
     * 总放行次数
     */
    public long getTotalPassCount() {
        return totalPassCount.get();
    }

    /**
     * 拦截次数 Top N 接口
     */
    public LinkedHashMap<String, Long> getTopBlockedApis(int n) {
        LinkedHashMap<String, Long> result = new LinkedHashMap<>();
        uriBlockCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(n)
                .forEach(e -> result.put(e.getKey(), e.getValue().get()));
        return result;
    }
}
