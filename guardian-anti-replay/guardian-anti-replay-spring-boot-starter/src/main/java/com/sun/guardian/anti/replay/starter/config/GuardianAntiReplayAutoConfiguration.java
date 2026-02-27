package com.sun.guardian.anti.replay.starter.config;

import com.sun.guardian.anti.replay.core.filter.AntiReplayFilter;
import com.sun.guardian.anti.replay.core.service.response.AntiReplayResponseHandler;
import com.sun.guardian.anti.replay.core.service.response.DefaultAntiReplayResponseHandler;
import com.sun.guardian.anti.replay.core.statistics.AntiReplayStatistics;
import com.sun.guardian.anti.replay.core.storage.NonceLocalStorage;
import com.sun.guardian.anti.replay.core.storage.NonceStorage;
import com.sun.guardian.anti.replay.starter.endpoint.AntiReplayEndPoint;
import com.sun.guardian.anti.replay.starter.properties.GuardianAntiReplayProperties;
import com.sun.guardian.core.config.GuardianCoreAutoConfiguration;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.storage.redis.replay.NonceRedisStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 防重放攻击模块自动配置
 * <p>
 * 注册 {@link AntiReplayFilter}、{@link NonceStorage}、{@link AntiReplayResponseHandler}、
 * {@link AntiReplayStatistics} 和 Actuator 监控端点。
 *
 * @author scj
 * @since 2026-02-27
 */
@Configuration
@EnableConfigurationProperties(GuardianAntiReplayProperties.class)
@ConditionalOnProperty(prefix = "guardian.anti-replay", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(GuardianCoreAutoConfiguration.class)
public class GuardianAntiReplayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AntiReplayFilter.class)
    public FilterRegistrationBean<AntiReplayFilter> antiReplayFilterRegistration(GuardianAntiReplayProperties properties,
                                                                                 NonceStorage storage,
                                                                                 AntiReplayResponseHandler responseHandler,
                                                                                 AntiReplayStatistics statistics,
                                                                                 GuardianMessageResolver messageResolver) {
        properties.validate();

        FilterRegistrationBean<AntiReplayFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AntiReplayFilter(properties, storage, responseHandler, statistics, messageResolver));
        registration.addUrlPatterns("/*");
        registration.setOrder(properties.getFilterOrder());
        return registration;
    }

    /**
     * Nonce Redis 存储（默认）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.anti-replay", name = "storage", havingValue = "redis", matchIfMissing = true)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnMissingBean(NonceStorage.class)
    public NonceRedisStorage nonceRedisStorage(StringRedisTemplate redisTemplate) {
        return new NonceRedisStorage(redisTemplate);
    }

    /**
     * Nonce 本地存储（guardian.anti-replay.storage=local 时启用）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.anti-replay", name = "storage", havingValue = "local")
    @ConditionalOnMissingBean(NonceStorage.class)
    public NonceLocalStorage nonceLocalStorage() {
        return new NonceLocalStorage();
    }

    /**
     * 默认 JSON 响应处理器
     */
    @Bean
    @ConditionalOnMissingBean(AntiReplayResponseHandler.class)
    public AntiReplayResponseHandler antiReplayResponseHandler() {
        return new DefaultAntiReplayResponseHandler();
    }

    /**
     * 防重放攻击统计组件
     */
    @Bean
    @ConditionalOnMissingBean(AntiReplayStatistics.class)
    public AntiReplayStatistics antiReplayStatistics() {
        return new AntiReplayStatistics();
    }

    /**
     * Actuator 监控端点（需引入 spring-boot-starter-actuator）
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    @ConditionalOnMissingBean(AntiReplayEndPoint.class)
    public AntiReplayEndPoint antiReplayEndPoint(AntiReplayStatistics statistics) {
        return new AntiReplayEndPoint(statistics);
    }
}
