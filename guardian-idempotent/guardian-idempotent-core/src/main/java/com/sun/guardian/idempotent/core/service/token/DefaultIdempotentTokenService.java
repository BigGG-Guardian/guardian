package com.sun.guardian.idempotent.core.service.token;

import com.sun.guardian.idempotent.core.config.IdempotentConfig;
import com.sun.guardian.idempotent.core.constants.IdempotentKeyPrefixConstants;
import com.sun.guardian.idempotent.core.domain.token.IdempotentToken;
import com.sun.guardian.idempotent.core.storage.IdempotentStorage;

/**
 * 默认接口幂等Token创建
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 15:30
 */
public class DefaultIdempotentTokenService implements IdempotentTokenService {
    private final IdempotentTokenGenerator tokenGenerator;
    private final IdempotentStorage idempotentStorage;
    private final IdempotentConfig idempotentConfig;

    /**
     * 构造默认幂等Token服务，持有配置接口引用以支持动态刷新
     *
     * @param tokenGenerator   Token 生成器
     * @param idempotentStorage 幂等存储
     * @param idempotentConfig  幂等配置（动态）
     */
    public DefaultIdempotentTokenService(IdempotentTokenGenerator tokenGenerator,
                                         IdempotentStorage idempotentStorage,
                                         IdempotentConfig idempotentConfig) {
        this.tokenGenerator = tokenGenerator;
        this.idempotentStorage = idempotentStorage;
        this.idempotentConfig = idempotentConfig;
    }

    @Override
    public String createToken(String key) {
        String token = tokenGenerator.generate();
        String fullKey = String.format(IdempotentKeyPrefixConstants.KEY_PREFIX, key, token);

        idempotentStorage.save(new IdempotentToken()
                .setKey(fullKey)
                .setCreateTime(System.currentTimeMillis())
                .setTimeout(idempotentConfig.getTimeout())
                .setTimeUnit(idempotentConfig.getTimeUnit()));
        return token;
    }
}
