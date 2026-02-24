package com.sun.guardian.core.config;

import com.sun.guardian.core.i18n.GuardianMessageResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 核心模块自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-24 16:30
 */
@Configuration
public class GuardianCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GuardianMessageResolver guardianMessageResolver(@Autowired(required = false) MessageSource messageSource) {
        return new GuardianMessageResolver(messageSource);
    }
}
