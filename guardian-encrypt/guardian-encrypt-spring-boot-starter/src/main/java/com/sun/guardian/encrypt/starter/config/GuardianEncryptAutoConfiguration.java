package com.sun.guardian.encrypt.starter.config;

import com.sun.guardian.encrypt.core.advice.EncryptResponseAdvice;
import com.sun.guardian.encrypt.core.enums.encrypt.DataEncryptAlgorithm;
import com.sun.guardian.encrypt.core.enums.encrypt.KeyEncryptAlgorithm;
import com.sun.guardian.encrypt.core.service.encrypt.DataEncryptService;
import com.sun.guardian.encrypt.core.service.encrypt.KeyEncryptService;
import com.sun.guardian.encrypt.starter.properties.GuardianEncryptProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 请求加密自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-09 13:00
 */
@Configuration
@ComponentScan(basePackages = "com.sun.guardian.encrypt.core.service.encrypt")
@EnableConfigurationProperties(GuardianEncryptProperties.class)
@ConditionalOnProperty(prefix = "guardian.encrypt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GuardianEncryptAutoConfiguration {

    /**
     * 结果加密 Advice
     */
    @Bean
    @ConditionalOnMissingBean(EncryptResponseAdvice.class)
    public EncryptResponseAdvice encryptResponseAdvice(GuardianEncryptProperties properties,
                                                       Map<KeyEncryptAlgorithm, KeyEncryptService> keyEncryptServiceMap,
                                                       Map<DataEncryptAlgorithm, DataEncryptService> dataEncryptServiceMap) {
        return new EncryptResponseAdvice(properties, keyEncryptServiceMap, dataEncryptServiceMap);
    }

    /**
     * key加密接口
     */
    @Bean
    public Map<KeyEncryptAlgorithm, KeyEncryptService> keyEncryptServiceMap(List<KeyEncryptService> services) {
        return services.stream()
                .collect(Collectors.toMap(
                        KeyEncryptService::getAlgorithm,
                        Function.identity()
                ));
    }

    /**
     * data加密接口
     */
    @Bean
    public Map<DataEncryptAlgorithm, DataEncryptService> dataEncryptServiceMap(List<DataEncryptService> services) {
        return services.stream()
                .collect(Collectors.toMap(
                        DataEncryptService::getAlgorithm,
                        Function.identity()
                ));
    }
}
