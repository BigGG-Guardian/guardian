package com.sun.guardian.idempotent.starter.properties;

import com.sun.guardian.core.properties.BaseGuardianProperties;
import com.sun.guardian.idempotent.core.config.IdempotentConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * 接口幂等配置参数
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-19 15:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "guardian.idempotent")
public class GuardianIdempotentProperties extends BaseGuardianProperties implements IdempotentConfig {

    /**
     * 总开关
     */
    private boolean enabled = true;

    /**
     * Token 有效期（默认 300）
     */
    private long timeout = 300;

    /**
     * Token 有效期单位（默认秒）
     */
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * 是否注册 Token 获取接口（默认 true，设 false 关闭内置接口）
     */
    private boolean tokenEndpoint = true;

    /**
     * 是否启用结果缓存（默认 false，启用后重复请求返回首次结果而非拒绝）
     */
    private boolean resultCache = false;

    public GuardianIdempotentProperties() {
        setInterceptorOrder(3000);
    }

    /**
     * 校验参数合法性，不合法时抛出 IllegalArgumentException
     */
    public void validate() {
        if (timeout <= 0) {
            throw new IllegalArgumentException("[Guardian-Idempotent] timeout 必须大于 0，当前值：" + timeout);
        }
    }
}
