package com.sun.guardian.trace.starter.config;

import com.sun.guardian.trace.core.filter.TraceIdFilter;
import com.sun.guardian.trace.starter.properties.GuardianTraceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 请求链路 TraceId 自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 19:42
 */
@Configuration
@EnableConfigurationProperties(GuardianTraceProperties.class)
@ConditionalOnProperty(prefix = "guardian.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GuardianTraceAutoConfiguration {

    /**
     * 注册 TraceId 过滤器
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter(GuardianTraceProperties properties) {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter(properties.getHeaderName()));
        registration.addUrlPatterns("/*");
        registration.setOrder(properties.getFilterOrder());
        registration.setName("traceIdFilter");
        return registration;
    }
}
