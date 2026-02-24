package com.sun.guardian.ip.filter.starter.config;

import com.sun.guardian.core.config.GuardianCoreAutoConfiguration;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.ip.filter.core.filter.IpFilter;
import com.sun.guardian.ip.filter.core.service.response.DefaultIpFilterResponseHandler;
import com.sun.guardian.ip.filter.core.service.response.IpFilterResponseHandler;
import com.sun.guardian.ip.filter.core.service.statistics.IpFilterStatistics;
import com.sun.guardian.ip.filter.starter.endpoint.IpFilterEndPoint;
import com.sun.guardian.ip.filter.starter.properties.GuardianIpFilterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * IP 黑白名单自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-24 21:29
 */
@Configuration
@ConditionalOnProperty(prefix = "guardian.ip-filter", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(GuardianIpFilterProperties.class)
@Import(GuardianCoreAutoConfiguration.class)
public class GuardianIpFilterAutoConfiguration {

    /**
     * 注册 IP 黑白名单过滤器
     */
    @Bean
    @ConditionalOnMissingBean(IpFilter.class)
    public FilterRegistrationBean<IpFilter> ipFilterFilterRegistration(
            GuardianIpFilterProperties properties, IpFilterResponseHandler responseHandler,
            IpFilterStatistics statistics, GuardianMessageResolver messageResolver) {
        FilterRegistrationBean<IpFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new IpFilter(properties, responseHandler, statistics, messageResolver));
        registration.addUrlPatterns("/*");
        registration.setOrder(properties.getFilterOrder());
        return registration;
    }

    /**
     * 默认 JSON 响应处理器
     */
    @Bean
    @ConditionalOnMissingBean(IpFilterResponseHandler.class)
    public IpFilterResponseHandler ipFilterResponseHandler() {
        return new DefaultIpFilterResponseHandler();
    }

    /**
     * IP 拦截统计组件
     */
    @Bean
    @ConditionalOnMissingBean(IpFilterStatistics.class)
    public IpFilterStatistics ipFilterStatistics() {
        return new IpFilterStatistics();
    }

    /**
     * Actuator 监控端点（需引入 spring-boot-starter-actuator）
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    @ConditionalOnMissingBean(IpFilterEndPoint.class)
    public IpFilterEndPoint ipFilterEndPoint(IpFilterStatistics statistics) {
        return new IpFilterEndPoint(statistics);
    }
}
