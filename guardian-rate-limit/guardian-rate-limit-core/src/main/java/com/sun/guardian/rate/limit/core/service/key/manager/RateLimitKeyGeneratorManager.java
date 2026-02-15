package com.sun.guardian.rate.limit.core.service.key.manager;

import com.sun.guardian.core.exception.KeyGeneratorNotFoundException;
import com.sun.guardian.rate.limit.core.service.key.RateLimitKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

/**
 * 限流键生成管理器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 21:11
 */
@RequiredArgsConstructor
public class RateLimitKeyGeneratorManager {

    private final ApplicationContext applicationContext;

    /** 获取限流键生成器 */
    public RateLimitKeyGenerator getKeyGenerator() {
        try {
            return applicationContext.getBean(RateLimitKeyGenerator.class);
        } catch (Exception e) {
            throw new KeyGeneratorNotFoundException("未找到接口限流键生成器");
        }
    }
}
