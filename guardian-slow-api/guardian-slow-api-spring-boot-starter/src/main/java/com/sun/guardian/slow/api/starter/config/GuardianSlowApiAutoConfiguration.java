package com.sun.guardian.slow.api.starter.config;

import com.sun.guardian.slow.api.core.interceptor.SlowApiInterceptor;
import com.sun.guardian.slow.api.core.recorder.DefaultSlowApiRecorder;
import com.sun.guardian.slow.api.core.recorder.SlowApiRecorder;
import com.sun.guardian.slow.api.starter.endpoint.SlowApiEndPoint;
import com.sun.guardian.slow.api.starter.properties.GuardianSlowApiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 慢接口检测自动配置
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 19:06
 */
@Configuration
@EnableConfigurationProperties(GuardianSlowApiProperties.class)
@ConditionalOnProperty(prefix = "guardian.slow-api", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GuardianSlowApiAutoConfiguration {

    /**
     * 注册慢接口检测拦截器到 WebMvc
     */
    @Configuration
    static class GuardianSlowApiWebMvcConfiguration implements WebMvcConfigurer {

        private final SlowApiInterceptor slowApiInterceptor;
        private final GuardianSlowApiProperties properties;

        /**
         * 构造 WebMvc 配置
         */
        public GuardianSlowApiWebMvcConfiguration(SlowApiInterceptor slowApiInterceptor, GuardianSlowApiProperties properties) {
            this.slowApiInterceptor = slowApiInterceptor;
            this.properties = properties;
        }

        /**
         * 添加慢接口检测拦截器
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(slowApiInterceptor).addPathPatterns("/**").order(properties.getInterceptorOrder());
        }
    }

    /**
     * 创建慢接口检测拦截器
     */
    @Bean
    @ConditionalOnMissingBean(SlowApiInterceptor.class)
    public SlowApiInterceptor slowApiInterceptor(GuardianSlowApiProperties properties, SlowApiRecorder recorder) {
        properties.validate();
        return new SlowApiInterceptor(properties, recorder);
    }

    @Bean
    @ConditionalOnMissingBean(SlowApiRecorder.class)
    public SlowApiRecorder slowApiRecorder() {
        return new DefaultSlowApiRecorder();
    }

    /**
     * 创建慢接口检测 Actuator 端点
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    @ConditionalOnMissingBean(SlowApiEndPoint.class)
    public SlowApiEndPoint slowApiEndPoint(SlowApiRecorder recorder) {
        return new SlowApiEndPoint(recorder);
    }
}
