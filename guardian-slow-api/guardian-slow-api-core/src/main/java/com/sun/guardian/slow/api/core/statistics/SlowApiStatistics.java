package com.sun.guardian.slow.api.core.statistics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 慢接口检测统计
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 18:39
 */
public class SlowApiStatistics {

    private final AtomicLong totalSlowCount = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> uriSlowCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> uriMaxDuration = new ConcurrentHashMap<>();

    /**
     * 获取慢接口触发总次数
     */
    public long getTotalSlowCount() {
        return totalSlowCount.get();
    }

    /**
     * 记录一次慢接口，更新次数和最大耗时
     */
    public void record(String uri, long duration) {
        totalSlowCount.incrementAndGet();
        uriSlowCount.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
        uriMaxDuration.merge(uri, duration, Math::max);
    }

    /**
     * 获取慢接口 Top N 排行
     */
    public LinkedHashMap<String, Map<String, Object>> getTopSlowApis(int n) {
        LinkedHashMap<String, Map<String, Object>> result = new LinkedHashMap<>();
        uriSlowCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(n)
                .forEach(e -> {
                    String uri = e.getKey();
                    long count = e.getValue().get();
                    long maxDuration = uriMaxDuration.containsKey(uri) ? uriMaxDuration.get(uri) : 0;

                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("count", count);
                    detail.put("maxDuration", maxDuration);
                    result.put(uri, detail);
                });
        return result;
    }
}
