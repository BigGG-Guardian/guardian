package com.sun.guardian.repeat.submit.core.service.statistics;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 防重复提交拦截统计
 * <p>
 * 基于内存的运行时统计，记录防重拦截次数和被拦截最多的接口。
 * 数据随 JVM 重启清零，定位为运行时监控指标，非持久化业务数据。
 * <p>
 * 线程安全：内部使用 {@link AtomicLong} 和 {@link ConcurrentHashMap}，
 * 支持高并发场景下的无锁统计。
 * <p>
 * 统计维度：
 * <ul>
 *   <li>{@link #recordBlock(String)} — 记录一次拦截（tryAcquire 失败时调用）</li>
 *   <li>{@link #recordPass()} — 记录一次放行（tryAcquire 成功时调用）</li>
 *   <li>{@link #getTopBlockedApis(int)} — 获取被拦截最多的 Top N 接口</li>
 * </ul>
 *
 * @author scj
 * @see com.sun.guardian.repeat.submit.core.interceptor.RepeatSubmitInterceptor
 * @since 2026-02-15
 */
public class RepeatSubmitStatistics {

    /**
     * 总拦截次数（tryAcquire 失败）
     */
    private final AtomicLong totalBlockCount = new AtomicLong(0);

    /**
     * 总放行次数（tryAcquire 成功）
     */
    private final AtomicLong totalPassCount = new AtomicLong(0);

    /**
     * 每个 URI 的拦截次数，key 为请求 URI
     */
    private final ConcurrentHashMap<String, AtomicLong> uriBlockCount = new ConcurrentHashMap<>();

    /**
     * 记录一次拦截
     * <p>
     * 在 {@code RepeatSubmitStorage.tryAcquire()} 返回 {@code false} 时调用，
     * 表示该请求被判定为重复提交并被拦截。
     *
     * @param uri 被拦截的请求 URI
     */
    public void recordBlock(String uri) {
        totalBlockCount.incrementAndGet();
        uriBlockCount.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 记录一次放行
     * <p>
     * 在 {@code RepeatSubmitStorage.tryAcquire()} 返回 {@code true} 时调用，
     * 表示该请求通过防重检查被正常放行。
     */
    public void recordPass() {
        totalPassCount.incrementAndGet();
    }

    /**
     * 获取总拦截次数
     *
     * @return 自服务启动以来的总拦截次数
     */
    public long getTotalBlockCount() {
        return totalBlockCount.get();
    }

    /**
     * 获取总放行次数
     *
     * @return 自服务启动以来的总放行次数
     */
    public long getTotalPassCount() {
        return totalPassCount.get();
    }

    /**
     * 获取被拦截最多的 Top N 接口
     * <p>
     * 按拦截次数降序排列，返回前 N 个接口的 URI 和对应拦截次数。
     *
     * @param n 返回前 N 条记录
     * @return 有序 Map，key 为 URI，value 为拦截次数，按次数降序排列
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
