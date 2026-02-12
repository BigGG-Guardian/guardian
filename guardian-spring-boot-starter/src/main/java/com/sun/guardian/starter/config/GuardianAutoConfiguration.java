package com.sun.guardian.starter.config;

import com.sun.guardian.core.context.UserContextResolver;
import com.sun.guardian.core.filter.RepeatableRequestFilter;
import com.sun.guardian.core.interceptor.RepeatSubmitInterceptor;
import com.sun.guardian.core.service.encrypt.manager.KeyEncryptManager;
import com.sun.guardian.core.service.encrypt.strategy.AbstractKeyEncrypt;
import com.sun.guardian.core.service.encrypt.strategy.KeyMD5Encrypt;
import com.sun.guardian.core.service.encrypt.strategy.KeyNoneEncrypt;
import com.sun.guardian.core.service.key.KeyGenerator;
import com.sun.guardian.core.service.key.manager.KeyGeneratorManager;
import com.sun.guardian.core.service.key.strategy.DefaultKeyGenerator;
import com.sun.guardian.core.storage.RepeatSubmitLocalStorage;
import com.sun.guardian.core.storage.RepeatSubmitStorage;
import com.sun.guardian.redis.storage.RepeatSubmitRedisStorage;
import com.sun.guardian.starter.properties.GuardianProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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
@ComponentScan(basePackages = "com.sun.guardian")
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
        return new RepeatSubmitInterceptor(keyGeneratorManager, repeatSubmitStorage, guardianProperties.getUrls());
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
