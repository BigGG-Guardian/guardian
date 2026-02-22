package com.sun.guardian.rate.limit.starter.config;

import com.sun.guardian.core.context.UserContext;
import com.sun.guardian.rate.limit.core.interceptor.RateLimitInterceptor;
import com.sun.guardian.rate.limit.core.service.key.RateLimitKeyGenerator;
import com.sun.guardian.rate.limit.core.service.key.strategy.DefaultRateLimitKeyGenerator;
import com.sun.guardian.rate.limit.core.service.response.DefaultRateLimitResponseHandler;
import com.sun.guardian.rate.limit.core.service.response.RateLimitResponseHandler;
import com.sun.guardian.rate.limit.core.statistics.RateLimitStatistics;
import com.sun.guardian.rate.limit.core.storage.RateLimitLocalStorage;
import com.sun.guardian.rate.limit.core.storage.RateLimitStorage;
import com.sun.guardian.rate.limit.starter.properties.GuardianRateLimitProperties;
import com.sun.guardian.rate.limit.starter.endpoint.RateLimitEndPoint;
import com.sun.guardian.storage.redis.rate.RateLimitRedisStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 接口限流自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 23:00
 */
@Configuration
@EnableConfigurationProperties(GuardianRateLimitProperties.class)
@ConditionalOnProperty(prefix = "guardian.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GuardianRateLimitAutoConfiguration {

    /**
     * 注册限流拦截器
     */
    @Configuration
    static class GuardianRateLimitWebMvcConfiguration implements WebMvcConfigurer {

        private final RateLimitInterceptor rateLimitInterceptor;
        private final GuardianRateLimitProperties properties;

        GuardianRateLimitWebMvcConfiguration(RateLimitInterceptor rateLimitInterceptor, GuardianRateLimitProperties properties) {
            this.rateLimitInterceptor = rateLimitInterceptor;
            this.properties = properties;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**").order(properties.getInterceptorOrder());
        }
    }

    /**
     * 接口限流拦截器
     */
    @Bean
    @ConditionalOnMissingBean(RateLimitInterceptor.class)
    public RateLimitInterceptor rateLimitInterceptor(RateLimitKeyGenerator keyGenerator,
                                                     RateLimitStorage rateLimitStorage,
                                                     RateLimitResponseHandler rateLimitResponseHandler,
                                                     GuardianRateLimitProperties guardianProperties,
                                                     RateLimitStatistics statistics) {
        return new RateLimitInterceptor(keyGenerator, rateLimitStorage, rateLimitResponseHandler, guardianProperties, statistics);
    }

    /**
     * Redis 存储（默认）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.rate-limit", name = "storage", havingValue = "redis", matchIfMissing = true)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnMissingBean(RateLimitStorage.class)
    public RateLimitRedisStorage rateLimitRedisStorage(StringRedisTemplate redisTemplate) {
        return new RateLimitRedisStorage(redisTemplate);
    }

    /**
     * 本地缓存存储（guardian.rate-limit.storage=local 时启用）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.rate-limit", name = "storage", havingValue = "local")
    @ConditionalOnMissingBean(RateLimitStorage.class)
    public RateLimitLocalStorage rateLimitLocalStorage() {
        return new RateLimitLocalStorage();
    }

    /**
     * 默认 JSON 响应处理器
     */
    @Bean
    @ConditionalOnMissingBean(RateLimitResponseHandler.class)
    public RateLimitResponseHandler rateLimitResponseHandler() {
        return new DefaultRateLimitResponseHandler();
    }

    /**
     * 默认用户上下文（返回 null，降级为 sessionId/IP）
     */
    @Bean
    @ConditionalOnMissingBean(UserContext.class)
    public UserContext rateLimitDefaultUserContext() {
        return () -> null;
    }

    /**
     * 默认限流键生成策略
     */
    @Bean
    @ConditionalOnMissingBean(RateLimitKeyGenerator.class)
    public DefaultRateLimitKeyGenerator defaultRateLimitKeyGenerator(UserContext userContext) {
        return new DefaultRateLimitKeyGenerator(userContext);
    }

    /**
     * 拦截统计
     */
    @Bean
    @ConditionalOnMissingBean(RateLimitStatistics.class)
    public RateLimitStatistics rateLimitStatistics() {
        return new RateLimitStatistics();
    }

    /**
     * Actuator 端点
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    @ConditionalOnMissingBean(RateLimitEndPoint.class)
    public RateLimitEndPoint rateLimitEndPoint(RateLimitStatistics statistics) {
        return new RateLimitEndPoint(statistics);
    }
}
