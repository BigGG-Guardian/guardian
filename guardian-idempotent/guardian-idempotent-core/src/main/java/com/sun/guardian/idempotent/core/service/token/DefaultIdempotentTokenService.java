package com.sun.guardian.idempotent.core.service.token;

import cn.hutool.core.util.StrUtil;
import com.sun.guardian.idempotent.core.constants.IdempotentKeyPrefixConstants;
import com.sun.guardian.idempotent.core.domain.token.IdempotentToken;
import com.sun.guardian.idempotent.core.storage.IdempotentStorage;

import java.util.concurrent.TimeUnit;

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
    private final long timeout;
    private final TimeUnit timeUnit;

    public DefaultIdempotentTokenService(IdempotentTokenGenerator tokenGenerator, IdempotentStorage idempotentStorage, long timeout, TimeUnit timeUnit) {
        this.tokenGenerator = tokenGenerator;
        this.idempotentStorage = idempotentStorage;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public String createToken(String key) {
        String token = tokenGenerator.generate();
        String fullKey = StrUtil.format(IdempotentKeyPrefixConstants.KEY_PREFIX, key, token);

        idempotentStorage.save(new IdempotentToken()
                .setKey(fullKey)
                .setCreateTime(System.currentTimeMillis())
                .setTimeout(timeout)
                .setTimeUnit(timeUnit));
        return token;
    }
}
