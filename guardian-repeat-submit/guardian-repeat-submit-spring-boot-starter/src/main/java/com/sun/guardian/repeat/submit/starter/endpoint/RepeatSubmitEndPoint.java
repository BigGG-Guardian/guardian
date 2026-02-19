package com.sun.guardian.repeat.submit.starter.endpoint;

import com.sun.guardian.repeat.submit.core.service.statistics.RepeatSubmitStatistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 防重 Actuator 端点（GET /actuator/guardianRepeatSubmit）
 *
 * @author scj
 */
@Endpoint(id = "guardianRepeatSubmit")
public class RepeatSubmitEndPoint {
    private final RepeatSubmitStatistics statistics;

    public RepeatSubmitEndPoint(RepeatSubmitStatistics statistics) {
        this.statistics = statistics;
    }

    /** 防重统计数据 */
    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalBlockCount", statistics.getTotalBlockCount());
        result.put("totalPassCount", statistics.getTotalPassCount());
        result.put("topBlockedApis", statistics.getTopBlockedApis(10));
        return result;
    }
}
