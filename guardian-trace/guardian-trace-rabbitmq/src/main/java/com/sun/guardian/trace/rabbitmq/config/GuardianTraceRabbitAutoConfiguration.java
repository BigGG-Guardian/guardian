package com.sun.guardian.trace.rabbitmq.config;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.rabbitmq.aspect.TraceRabbitListenerAspect;
import com.sun.guardian.trace.rabbitmq.processor.TraceRabbitMessagePostProcessor;
import com.sun.guardian.trace.rabbitmq.utils.TraceRabbitUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;

/**
 * RabbitMQ 链路追踪自动配置
 * 发送端：通过 MessagePostProcessor 注入 traceId（additive，不冲突）
 * 消费端：通过 AOP 切面拦截 @RabbitListener 方法（不占用 AdviceChain）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 20:16
 */
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnBean(TraceConfig.class)
@ConditionalOnProperty(prefix = "guardian.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(name = {
        "com.sun.guardian.trace.starter.config.GuardianTraceAutoConfiguration",
        "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class GuardianTraceRabbitAutoConfiguration {
    private final TraceConfig traceConfig;
    private final RabbitTemplate rabbitTemplate;

    public GuardianTraceRabbitAutoConfiguration(@Lazy TraceConfig traceConfig, RabbitTemplate rabbitTemplate) {
        this.traceConfig = traceConfig;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public TraceRabbitListenerAspect traceRabbitListenerAspect() {
        return new TraceRabbitListenerAspect(traceConfig);
    }

    @PostConstruct
    public void init() {
        rabbitTemplate.addBeforePublishPostProcessors(new TraceRabbitMessagePostProcessor(traceConfig));
        TraceRabbitUtils.setTraceConfig(traceConfig);
    }
}
