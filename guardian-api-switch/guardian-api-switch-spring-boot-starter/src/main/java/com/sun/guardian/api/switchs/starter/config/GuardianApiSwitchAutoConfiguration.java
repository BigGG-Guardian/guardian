package com.sun.guardian.api.switchs.starter.config;

import com.sun.guardian.api.switchs.core.interceptor.ApiSwitchInterceptor;
import com.sun.guardian.api.switchs.core.service.response.ApiSwitchResponseHandler;
import com.sun.guardian.api.switchs.core.service.response.DefaultApiSwitchResponseHandler;
import com.sun.guardian.api.switchs.starter.endpoint.ApiSwitchEndPoint;
import com.sun.guardian.api.switchs.starter.properties.GuardianApiSwitchProperties;
import com.sun.guardian.core.config.GuardianCoreAutoConfiguration;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 接口开关自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-28 22:18
 */
@Configuration
@EnableConfigurationProperties(GuardianApiSwitchProperties.class)
@ConditionalOnProperty(prefix = "guardian.api-switch", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(GuardianCoreAutoConfiguration.class)
public class GuardianApiSwitchAutoConfiguration {

    @Configuration
    static class GuardianApiSwitchWebMvcConfiguration implements WebMvcConfigurer {

        private final ApiSwitchInterceptor switchInterceptor;
        private final GuardianApiSwitchProperties properties;

        public GuardianApiSwitchWebMvcConfiguration(ApiSwitchInterceptor switchInterceptor, GuardianApiSwitchProperties properties) {
            this.switchInterceptor = switchInterceptor;
            this.properties = properties;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(switchInterceptor).addPathPatterns("/**").order(properties.getInterceptorOrder());
        }
    }

    /**
     * 接口开关拦截器
     */
    @Bean
    @ConditionalOnMissingBean(ApiSwitchInterceptor.class)
    public ApiSwitchInterceptor apiSwitchInterceptor(GuardianApiSwitchProperties properties,
                                                     ApiSwitchResponseHandler apiSwitchResponseHandler,
                                                     GuardianMessageResolver messageResolver) {

        return new ApiSwitchInterceptor(properties, apiSwitchResponseHandler, messageResolver);
    }

    /**
     * 默认 JSON 响应处理器
     */
    @Bean
    @ConditionalOnMissingBean(ApiSwitchResponseHandler.class)
    public ApiSwitchResponseHandler apiSwitchResponseHandler() {
        return new DefaultApiSwitchResponseHandler();
    }

    /**
     * Actuator 端点
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    @ConditionalOnMissingBean(ApiSwitchEndPoint.class)
    public ApiSwitchEndPoint apiSwitchEndPoint(GuardianApiSwitchProperties properties) {
        return new ApiSwitchEndPoint(properties);
    }
}
