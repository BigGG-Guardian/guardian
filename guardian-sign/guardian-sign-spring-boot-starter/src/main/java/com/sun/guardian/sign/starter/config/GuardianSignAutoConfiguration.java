package com.sun.guardian.sign.starter.config;

import com.sun.guardian.core.config.GuardianCoreAutoConfiguration;
import com.sun.guardian.core.filter.RepeatableRequestFilter;
import com.sun.guardian.core.i18n.GuardianMessageResolver;
import com.sun.guardian.core.properties.GuardianCoreProperties;
import com.sun.guardian.sign.core.advice.SignResultSignAdvice;
import com.sun.guardian.sign.core.interceptor.SignVerifyInterceptor;
import com.sun.guardian.sign.core.service.response.DefaultSignResponseHandler;
import com.sun.guardian.sign.core.service.response.SignResponseHandler;
import com.sun.guardian.sign.core.service.sign.DefaultSignService;
import com.sun.guardian.sign.core.service.sign.SignService;
import com.sun.guardian.sign.core.statistics.SignStatistics;
import com.sun.guardian.sign.starter.endpoint.SignEndPoint;
import com.sun.guardian.sign.starter.properties.GuardianSignProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 参数签名自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-03 14:46
 */
@Configuration
@EnableConfigurationProperties({GuardianSignProperties.class, GuardianCoreProperties.class})
@ConditionalOnProperty(prefix = "guardian.sign", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(GuardianCoreAutoConfiguration.class)
public class GuardianSignAutoConfiguration {

    @Configuration
    static class GuardianSignWebMvcConfiguration implements WebMvcConfigurer {

        private final SignVerifyInterceptor signVerifyInterceptor;
        private final GuardianSignProperties properties;

        public GuardianSignWebMvcConfiguration(SignVerifyInterceptor signVerifyInterceptor, GuardianSignProperties properties) {
            this.signVerifyInterceptor = signVerifyInterceptor;
            this.properties = properties;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(signVerifyInterceptor).addPathPatterns("/**").order(properties.getInterceptorOrder());
        }
    }

    /**
     * 请求体缓存过滤器
     */
    @Bean
    @ConditionalOnMissingBean(RepeatableRequestFilter.class)
    public FilterRegistrationBean<RepeatableRequestFilter> signRepeatableRequestFilterRegistration(GuardianCoreProperties coreProperties) {
        FilterRegistrationBean<RepeatableRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RepeatableRequestFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(coreProperties.getRepeatableFilterOrder());
        return registration;
    }

    /**
     * 参数签名拦截器
     */
    @Bean
    @ConditionalOnMissingBean(SignVerifyInterceptor.class)
    public SignVerifyInterceptor signVerifyInterceptor(GuardianSignProperties properties,
                                                       SignService signService,
                                                       SignResponseHandler responseHandler,
                                                       SignStatistics statistics,
                                                       GuardianMessageResolver messageResolver) {

        properties.validate();
        return new SignVerifyInterceptor(properties, signService, responseHandler, statistics, messageResolver);
    }

    /**
     * 签名 服务（可插拔，注册 Bean 替换）
     */
    @Bean
    @ConditionalOnMissingBean(SignService.class)
    public SignService signService() {
        return new DefaultSignService();
    }

    /**
     * 默认 JSON 响应处理器
     */
    @Bean
    @ConditionalOnMissingBean(SignResponseHandler.class)
    public SignResponseHandler signResponseHandler() {
        return new DefaultSignResponseHandler();
    }

    /**
     * 拦截统计
     */
    @Bean
    @ConditionalOnMissingBean(SignStatistics.class)
    public SignStatistics signStatistics() {
        return new SignStatistics();
    }

    /**
     * Actuator 端点
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    @ConditionalOnMissingBean(SignEndPoint.class)
    public SignEndPoint signEndPoint(SignStatistics statistics) {
        return new SignEndPoint(statistics);
    }

    /**
     * 结果签名配置（result-sign=true 时整体生效）
     */
    @Configuration
    @ConditionalOnProperty(prefix = "guardian.sign", name = "result-sign", havingValue = "true")
    static class SignResultSignConfiguration {

        /**
         * 结果签名 Advice
         */
        @Bean
        @ConditionalOnMissingBean(SignResultSignAdvice.class)
        public SignResultSignAdvice signResultSignAdvice(GuardianSignProperties properties,
                                                                SignService signService) {
            return new SignResultSignAdvice(properties, signService);
        }
    }

}
