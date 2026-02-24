package com.sun.guardian.ip.filter.starter.endpoint;

import com.sun.guardian.ip.filter.core.service.statistics.IpFilterStatistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * IP 黑白名单 Actuator 端点（GET /actuator/guardianIpFilter）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-24 21:34
 */
@Endpoint(id = "guardianIpFilter")
public class IpFilterEndPoint {

    private final IpFilterStatistics statistics;

    /**
     * 构造 Actuator 端点
     */
    public IpFilterEndPoint(IpFilterStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * 返回 IP 黑白名单拦截统计信息
     */
    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalBlackListBlockCount", statistics.getTotalBlackListBlockCount());
        result.put("totalWhiteListBlockCount", statistics.getTotalWhiteListBlockCount());
        result.put("topBlackListBlocked", statistics.getTopBlackListBlocked(10));
        result.put("topWhiteListBlocked", statistics.getTopWhiteListBlocked(10));
        return result;
    }
}
