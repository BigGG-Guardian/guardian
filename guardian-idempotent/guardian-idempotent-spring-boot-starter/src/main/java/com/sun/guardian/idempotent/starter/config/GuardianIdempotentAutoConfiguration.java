package com.sun.guardian.idempotent.starter.config;

import com.sun.guardian.core.config.GuardianCoreAutoConfiguration;
import com.sun.guardian.core.filter.RepeatableRequestFilter;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.idempotent.core.advice.IdempotentResultCacheAdvice;
import com.sun.guardian.idempotent.core.interceptor.IdempotentInterceptor;
import com.sun.guardian.idempotent.core.service.response.DefaultIdempotentResponseHandler;
import com.sun.guardian.idempotent.core.service.response.IdempotentResponseHandler;
import com.sun.guardian.idempotent.core.statistics.IdempotentStatistics;
import com.sun.guardian.idempotent.core.service.token.DefaultIdempotentTokenService;
import com.sun.guardian.idempotent.core.service.token.IdempotentTokenGenerator;
import com.sun.guardian.idempotent.core.service.token.IdempotentTokenService;
import com.sun.guardian.idempotent.core.service.token.strategy.DefaultIdempotentTokenGenerator;
import com.sun.guardian.idempotent.core.storage.IdempotentLocalResultCache;
import com.sun.guardian.idempotent.core.storage.IdempotentLocalStorage;
import com.sun.guardian.idempotent.core.storage.IdempotentResultCache;
import com.sun.guardian.idempotent.core.storage.IdempotentStorage;
import com.sun.guardian.idempotent.starter.controller.IdempotentTokenController;
import com.sun.guardian.idempotent.starter.endpoint.IdempotentEndPoint;
import com.sun.guardian.core.properties.GuardianCoreProperties;
import com.sun.guardian.idempotent.starter.properties.GuardianIdempotentProperties;
import com.sun.guardian.storage.redis.idempotent.IdempotentRedisResultCache;
import com.sun.guardian.storage.redis.idempotent.IdempotentRedisStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 接口幂等自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 17:09
 */
@Configuration
@EnableConfigurationProperties({GuardianIdempotentProperties.class, GuardianCoreProperties.class})
@ConditionalOnProperty(prefix = "guardian.idempotent", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(GuardianCoreAutoConfiguration.class)
public class GuardianIdempotentAutoConfiguration {

    @Configuration
    static class GuardianIdempotentWebMvcConfiguration implements WebMvcConfigurer {

        private final IdempotentInterceptor idempotentInterceptor;
        private final GuardianIdempotentProperties properties;

        public GuardianIdempotentWebMvcConfiguration(IdempotentInterceptor idempotentInterceptor, GuardianIdempotentProperties properties) {
            this.idempotentInterceptor = idempotentInterceptor;
            this.properties = properties;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(idempotentInterceptor).addPathPatterns("/**").order(properties.getInterceptorOrder());
        }
    }

    /**
     * 请求体缓存过滤器，使 JSON body 可重复读取（PARAM 模式解析 JSON Body Token 需要）
     */
    @Bean
    @ConditionalOnMissingBean(RepeatableRequestFilter.class)
    public FilterRegistrationBean<RepeatableRequestFilter> idempotentRepeatableRequestFilterRegistration(GuardianCoreProperties coreProperties) {
        FilterRegistrationBean<RepeatableRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RepeatableRequestFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(coreProperties.getRepeatableFilterOrder());
        return registration;
    }

    /**
     * 接口幂等拦截器
     */
    @Bean
    @ConditionalOnMissingBean(IdempotentInterceptor.class)
    public IdempotentInterceptor idempotentInterceptor(IdempotentStorage storage,
                                                       @Autowired(required = false) IdempotentResultCache resultCache,
                                                       IdempotentResponseHandler idempotentResponseHandler,
                                                       GuardianIdempotentProperties properties,
                                                       IdempotentStatistics statistics,
                                                       GuardianMessageResolver messageResolver) {

        properties.validate();
        return new IdempotentInterceptor(storage, resultCache, idempotentResponseHandler, properties, statistics, messageResolver);
    }

    /**
     * 接口幂等redis存储（默认）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.idempotent", name = "storage", havingValue = "redis", matchIfMissing = true)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnMissingBean(IdempotentStorage.class)
    public IdempotentRedisStorage idempotentRedisStorage(StringRedisTemplate redisTemplate) {
        return new IdempotentRedisStorage(redisTemplate);
    }

    /**
     * 接口幂等本地存储
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.idempotent", name = "storage", havingValue = "local")
    @ConditionalOnMissingBean(IdempotentStorage.class)
    public IdempotentLocalStorage idempotentLocalStorage() {
        return new IdempotentLocalStorage();
    }

    /**
     * Token 生成器（可插拔，注册 Bean 替换）
     */
    @Bean
    @ConditionalOnMissingBean(IdempotentTokenGenerator.class)
    public IdempotentTokenGenerator idempotentTokenGenerator() {
        return new DefaultIdempotentTokenGenerator();
    }

    /**
     * Token 服务
     */
    @Bean
    @ConditionalOnMissingBean(IdempotentTokenService.class)
    public IdempotentTokenService idempotentTokenService(IdempotentTokenGenerator tokenGenerator,
                                                         IdempotentStorage storage,
                                                         GuardianIdempotentProperties properties) {
        return new DefaultIdempotentTokenService(tokenGenerator, storage, properties);
    }

    /**
     * 默认 JSON 响应处理器
     */
    @Bean
    @ConditionalOnMissingBean(IdempotentResponseHandler.class)
    public IdempotentResponseHandler idempotentResponseHandler() {
        return new DefaultIdempotentResponseHandler();
    }

    /**
     * 拦截统计
     */
    @Bean
    @ConditionalOnMissingBean(IdempotentStatistics.class)
    public IdempotentStatistics idempotentStatistics() {
        return new IdempotentStatistics();
    }

    /**
     * Token 获取接口（token-endpoint=false 时关闭）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.idempotent", name = "token-endpoint", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(IdempotentTokenController.class)
    public IdempotentTokenController idempotentTokenController(IdempotentTokenService tokenService,
                                                               GuardianIdempotentProperties properties) {
        return new IdempotentTokenController(tokenService, properties);
    }

    /**
     * Actuator 端点
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    @ConditionalOnMissingBean(IdempotentEndPoint.class)
    public IdempotentEndPoint idempotentEndPoint(IdempotentStatistics statistics) {
        return new IdempotentEndPoint(statistics);
    }

    /**
     * 结果缓存配置（result-cache=true 时整体生效）
     */
    @Configuration
    @ConditionalOnProperty(prefix = "guardian.idempotent", name = "result-cache", havingValue = "true")
    static class IdempotentResultCacheConfiguration {

        /**
         * Redis 结果缓存（storage=redis，默认）
         */
        @Bean
        @ConditionalOnProperty(prefix = "guardian.idempotent", name = "storage", havingValue = "redis", matchIfMissing = true)
        @ConditionalOnClass(StringRedisTemplate.class)
        @ConditionalOnMissingBean(IdempotentResultCache.class)
        public IdempotentRedisResultCache idempotentResultRedisCache(StringRedisTemplate redisTemplate) {
            return new IdempotentRedisResultCache(redisTemplate);
        }

        /**
         * 本地结果缓存（storage=local）
         */
        @Bean
        @ConditionalOnProperty(prefix = "guardian.idempotent", name = "storage", havingValue = "local")
        @ConditionalOnMissingBean(IdempotentResultCache.class)
        public IdempotentLocalResultCache idempotentResultLocalCache() {
            return new IdempotentLocalResultCache();
        }

        /**
         * 结果缓存 Advice
         */
        @Bean
        @ConditionalOnMissingBean(IdempotentResultCacheAdvice.class)
        public IdempotentResultCacheAdvice idempotentResultCacheAdvice(IdempotentResultCache resultCache) {
            return new IdempotentResultCacheAdvice(resultCache);
        }
    }

}
