package com.sun.guardian.trace.kafka.config;

import com.sun.guardian.trace.core.config.TraceConfig;
import com.sun.guardian.trace.kafka.aspect.TraceKafkaListenerAspect;
import com.sun.guardian.trace.kafka.interceptor.TraceKafkaProducerInterceptor;
import com.sun.guardian.trace.kafka.utils.TraceKafkaUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;

import javax.annotation.PostConstruct;

/**
 * Kafka 链路追踪自动配置
 * <p>
 * 发送端：通过 ProducerInterceptor 注入 traceId，需接入方在 Kafka 配置中注册该拦截器
 * <br>
 * 消费端：通过 AOP 切面拦截 @KafkaListener 方法（不占用 RecordInterceptor / BatchInterceptor）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 21:07
 */
@Configuration
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnBean({TraceConfig.class, KafkaTemplate.class})
@ConditionalOnProperty(prefix = "guardian.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(name = {
        "com.sun.guardian.trace.starter.config.GuardianTraceAutoConfiguration",
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class GuardianTraceKafkaAutoConfiguration {
    private final TraceConfig traceConfig;

    public GuardianTraceKafkaAutoConfiguration(@Lazy TraceConfig traceConfig) {
        this.traceConfig = traceConfig;
    }

    @Bean
    public TraceKafkaListenerAspect traceKafkaListenerAspect() {
        return new TraceKafkaListenerAspect(traceConfig);
    }

    @PostConstruct
    public void init() {
        TraceKafkaProducerInterceptor.setTraceConfig(traceConfig);
        TraceKafkaUtils.setTraceConfig(traceConfig);
    }
}
