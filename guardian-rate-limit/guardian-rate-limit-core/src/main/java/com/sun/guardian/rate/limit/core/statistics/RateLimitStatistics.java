package com.sun.guardian.rate.limit.core.statistics;

import com.sun.guardian.core.statistics.BaseStatistics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 接口限流拦截统计
 *
 * 在 BaseStatistics 基础上增加：每 URI 请求量、命中率、Top N、各接口详情。
 *
 * @author scj
 * @version java version 1.8
 * @see com.sun.guardian.rate.limit.core.interceptor.RateLimitInterceptor
 * @since 2026-02-15
 */
public class RateLimitStatistics extends BaseStatistics {

    /**
     * 每个 URI 的总请求次数（包含放行和拦截）
     */
    private final ConcurrentHashMap<String, AtomicLong> uriRequestCount = new ConcurrentHashMap<>();

    /**
     * 记录一次拦截
     */
    @Override
    public void recordBlock(String uri) {
        super.recordBlock(uri);
        uriRequestCount.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 记录一次放行
     */
    public void recordPass(String uri) {
        super.recordPass();
        uriRequestCount.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 总请求次数（放行 + 拦截）
     */
    public long getTotalRequestCount() {
        return getTotalBlockCount() + getTotalPassCount();
    }

    /**
     * 限流命中率，如 "12.50%"
     */
    public String getBlockRate() {
        long total = getTotalRequestCount();
        if (total == 0) {
            return "0.00%";
        }
        double rate = (double) getTotalBlockCount() / total * 100;
        return String.format("%.2f%%", rate);
    }

    /**
     * 请求量 Top N 接口
     */
    public LinkedHashMap<String, Long> getTopRequestApis(int n) {
        LinkedHashMap<String, Long> result = new LinkedHashMap<>();
        uriRequestCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(n)
                .forEach(e -> result.put(e.getKey(), e.getValue().get()));
        return result;
    }

    /**
     * 各接口限流详情（请求数、拦截数、限流率）
     */
    public LinkedHashMap<String, Map<String, Object>> getApiDetails(int n) {
        LinkedHashMap<String, Map<String, Object>> result = new LinkedHashMap<>();
        uriRequestCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(n)
                .forEach(e -> {
                    String uri = e.getKey();
                    long requests = e.getValue().get();
                    long blocks = uriBlockCount.containsKey(uri) ? uriBlockCount.get(uri).get() : 0;
                    long passes = requests - blocks;
                    double rate = requests > 0 ? (double) blocks / requests * 100 : 0;

                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("requests", requests);
                    detail.put("passes", passes);
                    detail.put("blocks", blocks);
                    detail.put("blockRate", String.format("%.2f%%", rate));
                    result.put(uri, detail);
                });
        return result;
    }
}
