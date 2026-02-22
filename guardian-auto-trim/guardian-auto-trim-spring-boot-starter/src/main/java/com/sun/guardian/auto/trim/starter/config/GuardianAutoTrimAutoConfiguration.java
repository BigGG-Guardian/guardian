package com.sun.guardian.auto.trim.starter.config;

import com.sun.guardian.auto.trim.core.filter.AutoTrimFilter;
import com.sun.guardian.auto.trim.starter.properties.GuardianAutoTrimProperties;
import com.sun.guardian.core.utils.CharacterSanitizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 请求参数自动 trim 自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 15:32
 */
@Configuration
@EnableConfigurationProperties(GuardianAutoTrimProperties.class)
@ConditionalOnProperty(prefix = "guardian.auto-trim", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GuardianAutoTrimAutoConfiguration {

    /**
     * 注册参数自动 trim 过滤器
     */
    @Bean
    public FilterRegistrationBean<AutoTrimFilter> autoTrimFilter(GuardianAutoTrimProperties properties) {
        CharacterSanitizer sanitizer = new CharacterSanitizer(properties);
        FilterRegistrationBean<AutoTrimFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AutoTrimFilter(properties, sanitizer));
        registration.addUrlPatterns("/*");
        registration.setName("autoTrimFilter");
        registration.setOrder(properties.getFilterOrder());
        return registration;
    }
}
