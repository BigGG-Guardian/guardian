package com.sun.guardian.sign.starter.endpoint;

import com.sun.guardian.sign.core.statistics.SignStatistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.lang.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 参数签名 Actuator 端点（GET /actuator/guardianSign）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-03
 */
@Endpoint(id = "guardianSign")
public class SignEndPoint {
    private final SignStatistics statistics;

    /**
     * 构造参数签名 Actuator 端点
     */
    public SignEndPoint(SignStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * 返回参数签名监控数据
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
