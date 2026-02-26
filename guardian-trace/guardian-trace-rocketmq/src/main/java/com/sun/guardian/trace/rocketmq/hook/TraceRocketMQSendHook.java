package com.sun.guardian.trace.rocketmq.hook;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.core.filter.TraceIdFilter;
import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.slf4j.MDC;

/**
 * RocketMQ 发送端 Hook，将当前线程的 traceId 写入消息 UserProperty
 * registerSendMessageHook 支持注册多个 Hook，不冲突
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 21:31
 */
public class TraceRocketMQSendHook implements SendMessageHook {
    private final TraceConfig traceConfig;

    public TraceRocketMQSendHook(TraceConfig traceConfig) {
        this.traceConfig = traceConfig;
    }

    @Override
    public String hookName() {
        return "GuardianTraceSendHook";
    }

    @Override
    public void sendMessageBefore(SendMessageContext sendMessageContext) {
        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        if (traceId != null) {
            sendMessageContext.getMessage().putUserProperty(traceConfig.getHeaderName(), traceId);
        }
    }

    @Override
    public void sendMessageAfter(SendMessageContext sendMessageContext) {

    }
}
