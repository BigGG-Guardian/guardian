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
import com.sun.guardian.repeat.submit.core.service.response.DefaultRepeatSubmitResponseHandler;
import com.sun.guardian.repeat.submit.core.service.response.RepeatSubmitResponseHandler;
import com.sun.guardian.repeat.submit.core.service.statistics.RepeatSubmitStatistics;
import com.sun.guardian.repeat.submit.core.storage.RepeatSubmitLocalStorage;
import com.sun.guardian.repeat.submit.core.storage.RepeatSubmitStorage;
import com.sun.guardian.repeat.submit.redis.storage.RepeatSubmitRedisStorage;
import com.sun.guardian.repeat.submit.starter.endpoint.RepeatSubmitEndPoint;
import com.sun.guardian.repeat.submit.starter.properties.GuardianRepeatSubmitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
 * Guardian-Repeat-Submit 防重复提交自动配置类
 * <p>
 * 自动注册拦截器、过滤器及核心组件 Bean。
 * 所有 Bean 均标注 {@code @ConditionalOnMissingBean}，
 * 用户可通过注册同类型 Bean 覆盖默认实现。
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 23:00
 */
@Configuration
@EnableConfigurationProperties(GuardianRepeatSubmitProperties.class)
public class GuardianRepeatSubmitAutoConfiguration {

    /**
     * WebMvc 配置：注册防重拦截器
     * <p>
     * 独立为静态内部类，通过构造器注入 RepeatSubmitInterceptor，
     * 避免与外层 @Bean 定义产生循环依赖。
     */
    @Configuration
    static class GuardianRepeatSubmitWebMvcConfiguration implements WebMvcConfigurer {

        private final RepeatSubmitInterceptor repeatSubmitInterceptor;

        GuardianRepeatSubmitWebMvcConfiguration(RepeatSubmitInterceptor repeatSubmitInterceptor) {
            this.repeatSubmitInterceptor = repeatSubmitInterceptor;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
        }
    }

    /**
     * 注册请求体缓存过滤器，使 JSON 请求体可重复读取（拦截器中需要读取参数生成防重 Key）
     */
    @Bean
    public FilterRegistrationBean<RepeatableRequestFilter> repeatableRequestFilterRegistration() {
        FilterRegistrationBean<RepeatableRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RepeatableRequestFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(-1);
        return registration;
    }

    /**
     * 防重复提交拦截器
     */
    @Bean
    @ConditionalOnMissingBean(RepeatSubmitInterceptor.class)
    public RepeatSubmitInterceptor repeatSubmitInterceptor(KeyGeneratorManager keyGeneratorManager,
                                                           RepeatSubmitStorage repeatSubmitStorage,
                                                           RepeatSubmitResponseHandler repeatSubmitResponseHandler,
                                                           GuardianRepeatSubmitProperties guardianProperties,
                                                           RepeatSubmitStatistics statistics) {
        return new RepeatSubmitInterceptor(keyGeneratorManager, repeatSubmitStorage, repeatSubmitResponseHandler, guardianProperties.getUrls(), guardianProperties.getExcludeUrls(), guardianProperties.getResponseMode(), guardianProperties.isLogEnabled(), statistics);
    }

    /**
     * Redis 存储（默认）
     */
    @Bean
    @ConditionalOnProperty(prefix = "guardian.repeat-submit", name = "storage", havingValue = "redis", matchIfMissing = true)
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
     * 默认 JSON 响应处理器（用户未自定义时兜底）
     */
    @Bean
    @ConditionalOnMissingBean(RepeatSubmitResponseHandler.class)
    public RepeatSubmitResponseHandler repeatSubmitResponseHandler() {
        return new DefaultRepeatSubmitResponseHandler();
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
    @ConditionalOnProperty(prefix = "guardian.repeat-submit", name = "key-generator", havingValue = "default", matchIfMissing = true)
    @ConditionalOnMissingBean(KeyGenerator.class)
    public DefaultKeyGenerator defaultKeyGenerator(UserContextResolver userContextResolver, KeyEncryptManager keyEncryptManager) {
        return new DefaultKeyGenerator(userContextResolver, keyEncryptManager);
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
     * 拦截统计（内存，始终注册）
     */
    @Bean
    @ConditionalOnMissingBean(RepeatSubmitStatistics.class)
    public RepeatSubmitStatistics repeatSubmitStatistics() {
        return new RepeatSubmitStatistics();
    }

    /**
     * Actuator 端点（仅当用户引入了 spring-boot-starter-actuator 时才注册）
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    @ConditionalOnMissingBean(RepeatSubmitEndPoint.class)
    public RepeatSubmitEndPoint repeatSubmitEndPoint(RepeatSubmitStatistics statistics) {
        return new RepeatSubmitEndPoint(statistics);
    }
}
