package com.sun.guardian.encrypt.starter.config;

import com.sun.guardian.core.config.GuardianCoreAutoConfiguration;
import com.sun.guardian.core.filter.RepeatableRequestFilter;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.core.properties.GuardianCoreProperties;
import com.sun.guardian.encrypt.core.enums.encrypt.DataEncryptAlgorithm;
import com.sun.guardian.encrypt.core.enums.encrypt.KeyEncryptAlgorithm;
import com.sun.guardian.encrypt.core.filter.DecryptFilter;
import com.sun.guardian.encrypt.core.service.decrypt.DataDecryptService;
import com.sun.guardian.encrypt.core.service.decrypt.KeyDecryptService;
import com.sun.guardian.encrypt.core.service.response.DefaultEncryptResponseHandler;
import com.sun.guardian.encrypt.core.service.response.EncryptResponseHandler;
import com.sun.guardian.encrypt.starter.properties.GuardianDecryptProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 请求解密自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-09 12:33
 */
@Configuration
@ComponentScan(basePackages = "com.sun.guardian.encrypt.core.service.decrypt")
@EnableConfigurationProperties({GuardianDecryptProperties.class, GuardianCoreProperties.class})
@ConditionalOnProperty(prefix = "guardian.decrypt", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(GuardianCoreAutoConfiguration.class)
public class GuardianDecryptAutoConfiguration {

    /**
     * 解密拦截器
     */
    @Bean
    @ConditionalOnMissingBean(DecryptFilter.class)
    public FilterRegistrationBean<DecryptFilter> decryptFilterFilterRegistrationBean(GuardianDecryptProperties properties,
                                                                                     Map<KeyEncryptAlgorithm, KeyDecryptService> keyDecryptServiceMap,
                                                                                     Map<DataEncryptAlgorithm, DataDecryptService> dataDecryptServiceMap,
                                                                                     EncryptResponseHandler responseHandler,
                                                                                     GuardianMessageResolver messageResolver) {
        FilterRegistrationBean<DecryptFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new DecryptFilter(properties, keyDecryptServiceMap, dataDecryptServiceMap, responseHandler, messageResolver));
        registration.addUrlPatterns("/*");
        registration.setOrder(properties.getFilterOrder());
        return registration;
    }


    /**
     * 请求体缓存过滤器，使 JSON body 可重复读取（PARAM 模式解析 JSON Body Token 需要）
     */
    @Bean
    @ConditionalOnMissingBean(RepeatableRequestFilter.class)
    public FilterRegistrationBean<RepeatableRequestFilter> decryptRepeatableRequestFilterRegistration(GuardianCoreProperties coreProperties) {
        FilterRegistrationBean<RepeatableRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RepeatableRequestFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(coreProperties.getRepeatableFilterOrder());
        return registration;
    }

    /**
     * key解密接口
     */
    @Bean
    public Map<KeyEncryptAlgorithm, KeyDecryptService> keyDecryptServiceMap(List<KeyDecryptService> services) {
        return services.stream()
                .collect(Collectors.toMap(
                        KeyDecryptService::getAlgorithm,
                        Function.identity()
                ));
    }

    /**
     * data解密接口
     */
    @Bean
    public Map<DataEncryptAlgorithm, DataDecryptService> dataDecryptServiceMap(List<DataDecryptService> services) {
        return services.stream()
                .collect(Collectors.toMap(
                        DataDecryptService::getAlgorithm,
                        Function.identity()
                ));
    }

    /**
     * 默认 JSON 响应处理器
     */
    @Bean
    @ConditionalOnMissingBean(EncryptResponseHandler.class)
    public EncryptResponseHandler encryptResponseHandler() {
        return new DefaultEncryptResponseHandler();
    }

}
