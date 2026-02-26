package com.sun.guardian.slow.api.core.recorder;

import com.sun.guardian.slow.api.core.domain.record.SlowApiRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 默认慢接口记录器，基于内存环形缓冲区实现
 * 每个接口保留最近 maxSize 条记录，超出后自动淘汰最旧的记录
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 18:54
 */
public class DefaultSlowApiRecorder implements SlowApiRecorder {

    private static final int DEFAULT_MAX_SIZE = 100;

    private final int maxSize;
    private final AtomicLong totalSlowCount = new AtomicLong(0);
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<SlowApiRecord>> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> uriSlowCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> uriMaxDuration = new ConcurrentHashMap<>();

    public DefaultSlowApiRecorder() {
        this(DEFAULT_MAX_SIZE);
    }

    public DefaultSlowApiRecorder(int maxSize) {
        this.maxSize = maxSize;
    }


    @Override
    public void record(SlowApiRecord record) {
        totalSlowCount.incrementAndGet();
        uriSlowCount.computeIfAbsent(record.getUri(), k -> new AtomicLong(0)).incrementAndGet();
        uriMaxDuration.merge(record.getUri(), record.getDuration(), Math::max);

        ConcurrentLinkedDeque<SlowApiRecord> deque = store.computeIfAbsent(record.getUri(), k -> new ConcurrentLinkedDeque<>());
        deque.addFirst(record);
        while (deque.size() > maxSize) {
            deque.pollLast();
        }
    }

    @Override
    public long getTotalSlowCount() {
        return totalSlowCount.get();
    }

    @Override
    public LinkedHashMap<String, Map<String, Object>> getTopSlowApis(int n) {
        LinkedHashMap<String, Map<String, Object>> result = new LinkedHashMap<>();
        uriSlowCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(n)
                .forEach(e -> {
                    String uri = e.getKey();
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("count", e.getValue().get());
                    detail.put("maxDuration", uriMaxDuration.getOrDefault(uri, 0L));
                    result.put(uri, detail);
                });
        return result;
    }

    @Override
    public List<SlowApiRecord> getRecords(String uri, int limit) {
        ConcurrentLinkedDeque<SlowApiRecord> deque = store.get(uri);
        if (deque == null) {
            return Collections.emptyList();
        }

        return deque.stream().limit(limit).collect(Collectors.toList());
    }
}
