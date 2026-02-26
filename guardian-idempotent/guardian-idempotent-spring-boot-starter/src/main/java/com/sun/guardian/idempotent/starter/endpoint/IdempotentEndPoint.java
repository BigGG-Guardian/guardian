package com.sun.guardian.idempotent.starter.endpoint;

import com.sun.guardian.idempotent.core.statistics.IdempotentStatistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.lang.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 接口幂等 Actuator 端点（GET /actuator/guardianIdempotent）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 19:48
 */
@Endpoint(id = "guardianIdempotent")
public class IdempotentEndPoint {
    private final IdempotentStatistics statistics;

    /**
     * 构造幂等 Actuator 端点
     */
    public IdempotentEndPoint(IdempotentStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * 返回接口幂等监控数据
     *
     * @param top 被拦截接口 Top N，默认 10
     */
    @ReadOperation
    public Map<String, Object> info(@Nullable Integer top) {
        int n = (top != null && top > 0) ? top : 10;
        long block = statistics.getTotalBlockCount();
        long pass = statistics.getTotalPassCount();
        long total = block + pass;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRequestCount", total);
        result.put("totalPassCount", pass);
        result.put("totalBlockCount", block);
        result.put("blockRate", total == 0 ? "0.00%" : String.format("%.2f%%", block * 100.0 / total));
        result.put("topBlockedApis", statistics.getTopBlockedApis(n));
        return result;
    }
}
