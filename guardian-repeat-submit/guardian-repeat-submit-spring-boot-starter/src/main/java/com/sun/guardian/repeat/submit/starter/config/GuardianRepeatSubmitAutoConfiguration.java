package com.sun.guardian.repeat.submit.starter.config;

import com.sun.guardian.core.context.UserContext;
import com.sun.guardian.core.filter.RepeatableRequestFilter;
import com.sun.guardian.repeat.submit.core.interceptor.RepeatSubmitInterceptor;
import com.sun.guardian.repeat.submit.core.service.encrypt.strategy.AbstractKeyEncrypt;
import com.sun.guardian.repeat.submit.core.service.encrypt.strategy.KeyMD5Encrypt;
import com.sun.guardian.repeat.submit.core.service.encrypt.strategy.KeyNoneEncrypt;
import com.sun.guardian.repeat.submit.core.service.key.KeyGenerator;
import com.sun.guardian.repeat.submit.core.service.key.strategy.DefaultKeyGenerator;
import com.sun.guardian.repeat.submit.core.service.response.DefaultRepeatSubmitResponseHandler;
import com.sun.guardian.repeat.submit.core.service.response.RepeatSubmitResponseHandler;
import com.sun.guardian.repeat.submit.core.service.statistics.RepeatSubmitStatistics;
import com.sun.guardian.repeat.submit.core.storage.RepeatSubmitLocalStorage;
import com.sun.guardian.repeat.submit.core.storage.RepeatSubmitStorage;
import com.sun.guardian.storage.redis.repeat.RepeatSubmitRedisStorage;
import com.sun.guardian.repeat.submit.starter.endpoint.RepeatSubmitEndPoint;
import com.sun.guardian.core.properties.GuardianCoreProperties;
import com.sun.guardian.repeat.submit.starter.properties.GuardianRepeatSubmitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 防重复提交自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 23:00
 */
@Configuration
@ConditionalOnProperty(prefix = "guardian.repeat-submit", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({GuardianRepeatSubmitProperties.class, GuardianCoreProperties.class})
public class GuardianRepeatSubmitAutoConfiguration {

    /**
     * 注册防重拦截器
     */
    @Configuration
    static class GuardianRepeatSubmitWebMvcConfiguration implements WebMvcConfigurer {

        private final RepeatSubmitInterceptor repeatSubmitInterceptor;
        private final GuardianRepeatSubmitProperties properties;

        GuardianRepeatSubmitWebMvcConfiguration(RepeatSubmitInterceptor repeatSubmitInterceptor, GuardianRepeatSubmitProperties properties) {
            this.repeatSubmitInterceptor = repeatSubmitInterceptor;
            this.properties = properties;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**").order(properties.getInterceptorOrder());
        }
    }

    /**
     * 请求体缓存过滤器，使 JSON body 可重复读取
     */
    @Bean
    @ConditionalOnMissingBean(RepeatableRequestFilter.class)
    public FilterRegistrationBean<RepeatableRequestFilter> repeatableRequestFilterRegistration(GuardianCoreProperties coreProperties) {
        FilterRegistrationBean<RepeatableRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RepeatableRequestFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(coreProperties.getRepeatableFilterOrder());
        return registration;
    }

    /**
     * 防重复提交拦截器
     */
    @Bean
    @ConditionalOnMissingBean(RepeatSubmitInterceptor.class)
    public RepeatSubmitInterceptor repeatSubmitInterceptor(KeyGenerator keyGenerator,
                                                           RepeatSubmitStorage repeatSubmitStorage,
                                                           RepeatSubmitResponseHandler repeatSubmitResponseHandler,
                                                           GuardianRepeatSubmitProperties guardianProperties,
                                                           RepeatSubmitStatistics statistics) {
        return new RepeatSubmitInterceptor(keyGenerator, repeatSubmitStorage, repeatSubmitResponseHandler, guardianProperties, statistics);
    }

    /**
     * Redis 存储（默认）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.repeat-submit", name = "storage", havingValue = "redis", matchIfMissing = true)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnMissingBean(RepeatSubmitStorage.class)
    public RepeatSubmitRedisStorage repeatSubmitRedisStorage(StringRedisTemplate redisTemplate) {
        return new RepeatSubmitRedisStorage(redisTemplate);
    }

    /**
     * 本地缓存存储（guardian.storage=local 时启用）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.repeat-submit", name = "storage", havingValue = "local")
    @ConditionalOnMissingBean(RepeatSubmitStorage.class)
    public RepeatSubmitLocalStorage repeatSubmitLocalStorage() {
        return new RepeatSubmitLocalStorage();
    }

    /**
     * 默认 JSON 响应处理器
     */
    @Bean
    @ConditionalOnMissingBean(RepeatSubmitResponseHandler.class)
    public RepeatSubmitResponseHandler repeatSubmitResponseHandler() {
        return new DefaultRepeatSubmitResponseHandler();
    }

    /**
     * 默认用户上下文（返回 null，降级为 sessionId/IP）
     */
    @Bean
    @ConditionalOnMissingBean(UserContext.class)
    public UserContext defaultUserContext() {
        return () -> null;
    }

    /**
     * 默认防重键生成策略
     */
    @Bean
    @ConditionalOnMissingBean(KeyGenerator.class)
    public DefaultKeyGenerator defaultKeyGenerator(UserContext userContext, AbstractKeyEncrypt keyEncrypt) {
        return new DefaultKeyGenerator(userContext, keyEncrypt);
    }

    /**
     * 不加密策略（默认）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.repeat-submit", name = "key-encrypt", havingValue = "none", matchIfMissing = true)
    @ConditionalOnMissingBean(AbstractKeyEncrypt.class)
    public KeyNoneEncrypt keyNoneEncrypt() {
        return new KeyNoneEncrypt();
    }

    /**
     * MD5 加密策略（guardian.key-encrypt=md5 时启用）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.repeat-submit", name = "key-encrypt", havingValue = "md5")
    @ConditionalOnMissingBean(AbstractKeyEncrypt.class)
    public KeyMD5Encrypt keyMD5Encrypt() {
        return new KeyMD5Encrypt();
    }

    /**
     * 拦截统计
     */
    @Bean
    @ConditionalOnMissingBean(RepeatSubmitStatistics.class)
    public RepeatSubmitStatistics repeatSubmitStatistics() {
        return new RepeatSubmitStatistics();
    }

    /**
     * Actuator 端点
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    @ConditionalOnMissingBean(RepeatSubmitEndPoint.class)
    public RepeatSubmitEndPoint repeatSubmitEndPoint(RepeatSubmitStatistics statistics) {
        return new RepeatSubmitEndPoint(statistics);
    }
}
