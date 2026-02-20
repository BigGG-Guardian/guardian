package com.sun.guardian.rate.limit.starter.endpoint;

import com.sun.guardian.rate.limit.core.statistics.RateLimitStatistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 限流 Actuator 端点（GET /actuator/guardianRateLimit）
 *
 * @author scj
 * @version java version 1.8
 */
@Endpoint(id = "guardianRateLimit")
public class RateLimitEndPoint {
    private final RateLimitStatistics statistics;

    /**
     * 构造限流端点
     */
    public RateLimitEndPoint(RateLimitStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * 限流监控数据
     */
    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRequestCount", statistics.getTotalRequestCount());
        result.put("totalPassCount", statistics.getTotalPassCount());
        result.put("totalBlockCount", statistics.getTotalBlockCount());
        result.put("blockRate", statistics.getBlockRate());
        result.put("topBlockedApis", statistics.getTopBlockedApis(10));
        result.put("topRequestApis", statistics.getTopRequestApis(10));
        result.put("apiDetails", statistics.getApiDetails(10));
        return result;
    }
}
