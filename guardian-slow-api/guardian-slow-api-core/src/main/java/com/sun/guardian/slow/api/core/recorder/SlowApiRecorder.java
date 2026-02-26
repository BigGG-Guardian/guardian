package com.sun.guardian.slow.api.core.recorder;

import com.sun.guardian.slow.api.core.domain.record.SlowApiRecord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 慢接口记录器 SPI 接口
 * 默认提供内存环形缓冲实现，用户可通过注入自定义 Bean 替换为数据库 / ES 等持久化方案
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 18:51
 */
public interface SlowApiRecorder {

    /**
     * 记录一次慢接口调用
     *
     * @param record 慢接口记录
     */
    void record(SlowApiRecord record);

    /**
     * 获取慢接口触发总次数
     */
    long getTotalSlowCount();

    /**
     * 获取慢接口 Top N 排行（按触发次数倒序）
     *
     * @param n 返回条数
     * @return key=URI，value=该接口的统计摘要（count / maxDuration）
     */
    LinkedHashMap<String, Map<String, Object>> getTopSlowApis(int n);

    /**
     * 查询指定接口的最近慢调用记录
     *
     * @param uri   接口路径
     * @param limit 返回条数
     * @return 按时间倒序排列的记录列表
     */
    List<SlowApiRecord> getRecords(String uri, int limit);
}
