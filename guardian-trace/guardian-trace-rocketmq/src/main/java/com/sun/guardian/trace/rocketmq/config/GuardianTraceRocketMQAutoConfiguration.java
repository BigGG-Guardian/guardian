package com.sun.guardian.trace.rocketmq.config;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.rocketmq.aspect.TraceRocketMQListenerAspect;
import com.sun.guardian.trace.rocketmq.hook.TraceRocketMQSendHook;
import com.sun.guardian.trace.rocketmq.utils.TraceRocketMQUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;

/**
 * RocketMQ 链路追踪自动配置
 * <p>
 * 发送端：通过 SendMessageHook 注入 traceId（支持注册多个 Hook，不冲突）
 * <br>
 * 消费端：通过 AOP 切面拦截 @RocketMQMessageListener 注解的类（不占用 ConsumeMessageHook）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 21:34
 */
@Configuration
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnBean({TraceConfig.class, RocketMQTemplate.class})
@ConditionalOnProperty(prefix = "guardian.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(name = {
        "com.sun.guardian.trace.starter.config.GuardianTraceAutoConfiguration",
        "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration"
})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class GuardianTraceRocketMQAutoConfiguration {
    private final TraceConfig traceConfig;
    private final RocketMQTemplate rocketMQTemplate;

    public GuardianTraceRocketMQAutoConfiguration(@Lazy TraceConfig traceConfig,
                                                  RocketMQTemplate rocketMQTemplate) {
        this.traceConfig = traceConfig;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Bean
    public TraceRocketMQListenerAspect traceRocketMQListenerAspect() {
        return new TraceRocketMQListenerAspect(traceConfig);
    }

    @PostConstruct
    public void init() {
        rocketMQTemplate.getProducer().getDefaultMQProducerImpl()
                .registerSendMessageHook(new TraceRocketMQSendHook(traceConfig));
        TraceRocketMQUtils.setTraceConfig(traceConfig);
    }
}
