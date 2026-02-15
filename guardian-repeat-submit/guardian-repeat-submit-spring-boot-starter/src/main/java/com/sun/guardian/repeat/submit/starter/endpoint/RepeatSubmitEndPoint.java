package com.sun.guardian.repeat.submit.starter.endpoint;

import com.sun.guardian.repeat.submit.core.service.statistics.RepeatSubmitStatistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Guardian-Repeat-Submit Actuator 端点
 * <p>
 * 访问 GET /actuator/guardian-repeat-submit 查看拦截统计数据。
 * <p>
 * 需要在 application.yml 中暴露端点：
 * <pre>
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: guardian-repeat-submit
 * </pre>
 *
 * @author scj
 */
@Endpoint(id = "guardian-repeat-submit")
public class RepeatSubmitEndPoint {
    private final RepeatSubmitStatistics statistics;

    public RepeatSubmitEndPoint(RepeatSubmitStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * GET /actuator/guardian-repeat-submit
     * <p>
     * 返回示例：
     * <pre>
     * {
     *   "totalBlockCount": 128,
     *   "totalPassCount": 5432,
     *   "topBlockedApis": {
     *     "/api/order/submit": 56,
     *     "/api/sms/send": 42
     *   }
     * }
     * </pre>
     */
    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalBlockCount", statistics.getTotalBlockCount());
        result.put("totalPassCount", statistics.getTotalPassCount());
        result.put("topBlockedApis", statistics.getTopBlockedApis(10));
        return result;
    }
}
