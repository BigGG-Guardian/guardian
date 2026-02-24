package com.sun.guardian.ip.filter.core.service.statistics;

import com.sun.guardian.core.statistics.BaseStatistics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * IP 黑白名单拦截统计（内存，JVM 重启清零）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15
 */
public class IpFilterStatistics extends BaseStatistics {

    private final AtomicLong totalBlackListBlockCount = new AtomicLong(0);
    private final AtomicLong totalWhiteListBlockCount = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> blackListBlockDetail = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> whiteListBlockDetail = new ConcurrentHashMap<>();

    /**
     * 记录一次黑名单拦截
     */
    public void recordBlackListBlock(String ip) {
        totalBlackListBlockCount.incrementAndGet();
        blackListBlockDetail.computeIfAbsent(ip, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 记录一次白名单拦截（IP 不在白名单内被拒绝）
     */
    public void recordWhiteListBlock(String ip, String uri) {
        totalWhiteListBlockCount.incrementAndGet();
        whiteListBlockDetail.computeIfAbsent(uri + " | " + ip, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 黑名单累计拦截次数
     */
    public long getTotalBlackListBlockCount() {
        return totalBlackListBlockCount.get();
    }

    /**
     * 白名单累计拦截次数
     */
    public long getTotalWhiteListBlockCount() {
        return totalWhiteListBlockCount.get();
    }

    /**
     * 黑名单拦截 Top N（按 IP 维度）
     */
    public Map<String, Long> getTopBlackListBlocked(int n) {
        return blackListBlockDetail.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(n)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(), (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * 白名单拦截 Top N（按 URI + IP 维度）
     */
    public Map<String, Long> getTopWhiteListBlocked(int n) {
        return whiteListBlockDetail.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(n)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(), (a, b) -> a, LinkedHashMap::new));
    }
}
