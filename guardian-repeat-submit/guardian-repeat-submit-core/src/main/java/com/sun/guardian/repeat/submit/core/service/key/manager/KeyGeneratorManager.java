package com.sun.guardian.repeat.submit.core.service.key.manager;

import com.sun.guardian.core.exception.KeyGeneratorNotFoundException;
import com.sun.guardian.repeat.submit.core.service.key.KeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

/**
 * 防重键生成管理器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 21:11
 */
@RequiredArgsConstructor
public class KeyGeneratorManager {

    private final ApplicationContext applicationContext;

    /**
     * 获取 yml 配置的防重键生成器
     */
    public KeyGenerator getKeyGenerator() {
        try {
            return applicationContext.getBean(KeyGenerator.class);
        } catch (Exception e) {
            throw new KeyGeneratorNotFoundException("未找到防重键生成器");
        }
    }
}
