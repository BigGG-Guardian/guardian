package com.sun.guardian.repeat.submit.core.service.encrypt.manager;

import com.sun.guardian.core.exception.KeyEncryptNotFoundException;
import com.sun.guardian.repeat.submit.core.service.encrypt.strategy.AbstractKeyEncrypt;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

/**
 * 防重键加密管理器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 21:11
 */
@RequiredArgsConstructor
public class KeyEncryptManager {

    private final ApplicationContext applicationContext;

    /**
     * 获取 yml 配置的加密器
     */
    public AbstractKeyEncrypt getKeyEncrypt() {
        try {
            return applicationContext.getBean(AbstractKeyEncrypt.class);
        } catch (Exception e) {
            throw new KeyEncryptNotFoundException("未找到防重键加密器");
        }
    }
}
