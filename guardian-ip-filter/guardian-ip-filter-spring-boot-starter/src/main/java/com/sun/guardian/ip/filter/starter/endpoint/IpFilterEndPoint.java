package com.sun.guardian.ip.filter.starter.endpoint;

import com.sun.guardian.ip.filter.core.service.statistics.IpFilterStatistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.lang.Nullable;

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
     * 返回 IP 黑白名单监控数据
     *
     * @param blockTop 黑名单拦截 IP Top N，默认 10
     * @param whiteTop 白名单拦截请求 Top N，默认 10
     */
    @ReadOperation
    public Map<String, Object> info(@Nullable Integer blockTop, @Nullable Integer whiteTop) {
        int blockN = (blockTop != null && blockTop > 0) ? blockTop : 10;
        int whiteN = (whiteTop != null && whiteTop > 0) ? whiteTop : 10;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalBlackListBlockCount", statistics.getTotalBlackListBlockCount());
        result.put("totalWhiteListBlockCount", statistics.getTotalWhiteListBlockCount());
        result.put("topBlackListBlocked", statistics.getTopBlackListBlocked(blockN));
        result.put("topWhiteListBlocked", statistics.getTopWhiteListBlocked(whiteN));
        return result;
    }
}
