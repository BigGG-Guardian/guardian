package com.sun.guardian.slow.api.starter.endpoint;


import com.sun.guardian.slow.api.core.domain.record.SlowApiRecord;
import com.sun.guardian.slow.api.core.recorder.SlowApiRecorder;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.lang.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 慢接口检测 Actuator 端点（GET /actuator/guardianSlowApi）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20
 */
@Endpoint(id = "guardianSlowApi")
public class SlowApiEndPoint {
    private final SlowApiRecorder recorder;

    /**
     * 构造慢接口检测端点
     */
    public SlowApiEndPoint(SlowApiRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * 返回慢接口检测监控数据
     * 请求示例：GET /actuator/guardianSlowApi?top=20&recordTop=10
     *
     * @param top       返回 Top N 条，默认 10
     * @param recordTop 返回详细记录 Top N 条，默认 5,最大100
     */
    @ReadOperation
    public Map<String, Object> info(@Nullable Integer top, @Nullable Integer recordTop) {
        int n = (top != null && top > 0) ? top : 10;
        int recordN = (recordTop != null && recordTop > 0) ? recordTop : 5;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSlowCount", recorder.getTotalSlowCount());
        LinkedHashMap<String, Map<String, Object>> topApis = recorder.getTopSlowApis(n);
        for (Map.Entry<String, Map<String, Object>> entry : topApis.entrySet()) {
            List<SlowApiRecord> records = recorder.getRecords(entry.getKey(), recordN);
            List<Map<String, Object>> recentList = new ArrayList<>();
            for (SlowApiRecord r : records) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("duration", r.getDuration());
                item.put("time", sdf.format(new Date(r.getTimestamp())));
                recentList.add(item);
            }
            entry.getValue().put("recentRecords", recentList);
        }

        result.put("topSlowApis", topApis);
        return result;
    }
}
