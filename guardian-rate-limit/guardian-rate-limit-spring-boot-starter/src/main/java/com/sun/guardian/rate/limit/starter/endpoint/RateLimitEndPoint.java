package com.sun.guardian.rate.limit.starter.endpoint;

import com.sun.guardian.rate.limit.core.statistics.RateLimitStatistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.lang.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 接口限流 Actuator 端点（GET /actuator/guardianRateLimit）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20
 */
@Endpoint(id = "guardianRateLimit")
public class RateLimitEndPoint {
    private final RateLimitStatistics statistics;

    /**
     * 构造限流 Actuator 端点
     */
    public RateLimitEndPoint(RateLimitStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * 返回接口限流监控数据
     *
     * @param blockedTop  被拦截接口 Top N，默认 10
     * @param requestTop  高频请求接口 Top N，默认 10
     * @param detailsTop  接口维度明细 Top N，默认 10
     */
    @ReadOperation
    public Map<String, Object> info(@Nullable Integer blockedTop, @Nullable Integer requestTop, @Nullable Integer detailsTop) {
        int blockedN = (blockedTop != null && blockedTop > 0) ? blockedTop : 10;
        int requestN = (requestTop != null && requestTop > 0) ? requestTop : 10;
        int detailsN = (detailsTop != null && detailsTop > 0) ? detailsTop : 10;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRequestCount", statistics.getTotalRequestCount());
        result.put("totalPassCount", statistics.getTotalPassCount());
        result.put("totalBlockCount", statistics.getTotalBlockCount());
        result.put("blockRate", statistics.getBlockRate());
        result.put("topBlockedApis", statistics.getTopBlockedApis(blockedN));
        result.put("topRequestApis", statistics.getTopRequestApis(requestN));
        result.put("apiDetails", statistics.getApiDetails(detailsN));
        return result;
    }
}
