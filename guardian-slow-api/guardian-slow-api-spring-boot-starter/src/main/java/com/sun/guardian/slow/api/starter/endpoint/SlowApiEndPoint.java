package com.sun.guardian.slow.api.starter.endpoint;


import com.sun.guardian.slow.api.core.statistics.SlowApiStatistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 慢接口检测 Actuator 端点（GET /actuator/guardianSlowApi）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20
 */
@Endpoint(id = "guardianSlowApi")
public class SlowApiEndPoint {
    private final SlowApiStatistics statistics;

    /**
     * 构造慢接口检测端点
     */
    public SlowApiEndPoint(SlowApiStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * 返回慢接口检测监控数据
     */
    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSlowCount", statistics.getTotalSlowCount());
        result.put("topSlowApis", statistics.getTopSlowApis(10));
        return result;
    }
}
