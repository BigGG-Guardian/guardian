package com.sun.guardian.trace.rocketmq.utils;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.core.support.utils.TraceUtils;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * RocketMQ 批量消费场景 traceId 切换工具
 * <p>
 * RocketMQ Spring Boot Starter 的 {@code RocketMQListener} 本身是逐条回调，
 * AOP 切面已为每次调用自动注入 traceId。如需手动切换，可调用 {@link #switchTraceId(MessageExt)}：
 * <pre>
 * TraceRocketMQUtils.switchTraceId(messageExt);
 * // 业务逻辑...
 * </pre>
 *
 * @author scj
 * @since 2026-02-09
 */
public final class TraceRocketMQUtils {

    private static volatile TraceConfig traceConfig;

    private TraceRocketMQUtils() {
    }

    public static void setTraceConfig(TraceConfig config) {
        traceConfig = config;
    }

    /**
     * 从 RocketMQ {@link MessageExt} UserProperty 中提取 traceId 并切换到当前线程 MDC
     *
     * @param message RocketMQ 消息
     */
    public static void switchTraceId(MessageExt message) {
        if (traceConfig == null || message == null) {
            return;
        }
        String traceId = message.getUserProperty(traceConfig.getHeaderName());
        TraceUtils.switchTraceId(traceId);
    }
}
