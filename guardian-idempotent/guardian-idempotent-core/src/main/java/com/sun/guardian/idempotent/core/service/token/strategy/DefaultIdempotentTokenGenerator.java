package com.sun.guardian.idempotent.core.service.token.strategy;

import cn.hutool.core.lang.UUID;
import com.sun.guardian.idempotent.core.service.token.IdempotentTokenGenerator;

/**
 * 默认接口幂等Token生成逻辑
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-18 15:17
 */
public class DefaultIdempotentTokenGenerator implements IdempotentTokenGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
