package com.sun.guardian.repeat.submit.starter.endpoint;

import com.sun.guardian.repeat.submit.core.service.statistics.RepeatSubmitStatistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.lang.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 防重 Actuator 端点（GET /actuator/guardianRepeatSubmit）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20
 */
@Endpoint(id = "guardianRepeatSubmit")
public class RepeatSubmitEndPoint {
    private final RepeatSubmitStatistics statistics;

    /**
     * 构造防重 Actuator 端点
     */
    public RepeatSubmitEndPoint(RepeatSubmitStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * 返回防重复提交监控数据
     *
     * @param top 被拦截接口 Top N，默认 10
     */
    @ReadOperation
    public Map<String, Object> info(@Nullable Integer top) {
        int n = (top != null && top > 0) ? top : 10;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalBlockCount", statistics.getTotalBlockCount());
        result.put("totalPassCount", statistics.getTotalPassCount());
        result.put("topBlockedApis", statistics.getTopBlockedApis(n));
        return result;
    }
}
