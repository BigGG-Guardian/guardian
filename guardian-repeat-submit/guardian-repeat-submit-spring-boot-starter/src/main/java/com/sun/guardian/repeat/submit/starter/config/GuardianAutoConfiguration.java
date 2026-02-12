package com.sun.guardian.repeat.submit.starter.config;

import com.sun.guardian.repeat.submit.core.context.UserContextResolver;
import com.sun.guardian.repeat.submit.core.filter.RepeatableRequestFilter;
import com.sun.guardian.repeat.submit.core.interceptor.RepeatSubmitInterceptor;
import com.sun.guardian.repeat.submit.core.service.encrypt.manager.KeyEncryptManager;
import com.sun.guardian.repeat.submit.core.service.encrypt.strategy.AbstractKeyEncrypt;
import com.sun.guardian.repeat.submit.core.service.encrypt.strategy.KeyMD5Encrypt;
import com.sun.guardian.repeat.submit.core.service.encrypt.strategy.KeyNoneEncrypt;
import com.sun.guardian.repeat.submit.core.service.key.KeyGenerator;
import com.sun.guardian.repeat.submit.core.service.key.manager.KeyGeneratorManager;
import com.sun.guardian.repeat.submit.core.service.key.strategy.DefaultKeyGenerator;
import com.sun.guardian.repeat.submit.core.storage.RepeatSubmitLocalStorage;
import com.sun.guardian.repeat.submit.core.storage.RepeatSubmitStorage;
import com.sun.guardian.repeat.submit.redis.storage.RepeatSubmitRedisStorage;
import com.sun.guardian.repeat.submit.starter.properties.GuardianProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Guardian 自动配置类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 23:00
 */
@Configuration
@EnableConfigurationProperties(GuardianProperties.class)
public class GuardianAutoConfiguration implements WebMvcConfigurer {

    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    }

    @Bean
    public FilterRegistrationBean<RepeatableRequestFilter> repeatableRequestFilterRegistration() {
        FilterRegistrationBean<RepeatableRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RepeatableRequestFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(-1);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(RepeatSubmitInterceptor.class)
    public RepeatSubmitInterceptor repeatSubmitInterceptor(KeyGeneratorManager keyGeneratorManager,
                                                           RepeatSubmitStorage repeatSubmitStorage,
                                                           GuardianProperties guardianProperties) {
        return new RepeatSubmitInterceptor(keyGeneratorManager, repeatSubmitStorage, guardianProperties.getUrls(), guardianProperties.getExcludeUrls());
    }

    /**
     * Redis 存储（默认）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian", name = "storage", havingValue = "redis", matchIfMissing = true)
    @ConditionalOnMissingBean(RepeatSubmitStorage.class)
    public RepeatSubmitRedisStorage repeatSubmitRedisStorage(StringRedisTemplate redisTemplate) {
        return new RepeatSubmitRedisStorage(redisTemplate);
    }

    /**
     * 本地缓存存储（guardian.storage=local 时启用）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian", name = "storage", havingValue = "local")
    @ConditionalOnMissingBean(RepeatSubmitStorage.class)
    public RepeatSubmitLocalStorage repeatSubmitLocalStorage() {
        return new RepeatSubmitLocalStorage();
    }

    /**
     * 防重键生成管理器
     */
    @Bean
    @ConditionalOnMissingBean(KeyGeneratorManager.class)
    public KeyGeneratorManager keyGeneratorManager(ApplicationContext applicationContext) {
        return new KeyGeneratorManager(applicationContext);
    }

    /**
     * 防重键加密管理器
     */
    @Bean
    @ConditionalOnMissingBean(KeyEncryptManager.class)
    public KeyEncryptManager keyEncryptManager(ApplicationContext applicationContext) {
        return new KeyEncryptManager(applicationContext);
    }

    /**
     * 默认用户上下文解析器（用户未自定义时兜底，返回 null 触发 sessionId/IP 降级）
     */
    @Bean
    @ConditionalOnMissingBean(UserContextResolver.class)
    public UserContextResolver defaultUserContextResolver() {
        return () -> null;
    }

    /**
     * 默认生成策略（guardian.key-generator=default 或未配置时启用）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian", name = "key-generator", havingValue = "default", matchIfMissing = true)
    @ConditionalOnMissingBean(KeyGenerator.class)
    public DefaultKeyGenerator defaultKeyGenerator(UserContextResolver userContextResolver, KeyEncryptManager keyEncryptManager) {
        return new DefaultKeyGenerator(userContextResolver, keyEncryptManager);
    }

    /**
     * 不加密策略（默认）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian", name = "key-encrypt", havingValue = "none", matchIfMissing = true)
    @ConditionalOnMissingBean(AbstractKeyEncrypt.class)
    public KeyNoneEncrypt keyNoneEncrypt() {
        return new KeyNoneEncrypt();
    }

    /**
     * MD5 加密策略（guardian.key-encrypt=md5 时启用）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian", name = "key-encrypt", havingValue = "md5")
    @ConditionalOnMissingBean(AbstractKeyEncrypt.class)
    public KeyMD5Encrypt keyMD5Encrypt() {
        return new KeyMD5Encrypt();
    }
}
