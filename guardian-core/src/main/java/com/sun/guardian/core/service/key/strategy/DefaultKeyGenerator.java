package com.sun.guardian.core.service.key.strategy;

import cn.hutool.core.util.StrUtil;
import com.sun.guardian.core.context.UserContextResolver;
import com.sun.guardian.core.domain.key.RepeatSubmitKey;
import com.sun.guardian.core.service.encrypt.manager.KeyEncryptManager;
import static com.sun.guardian.core.constants.KeyPrefixConstants.DEFAULT_KEY_SUFFIX;

/**
 * 防重键默认生成策略
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 20:01
 */
public class DefaultKeyGenerator extends AbstractKeyGenerator {

    public DefaultKeyGenerator(UserContextResolver userContextResolver, KeyEncryptManager keyEncryptManager) {
        super(userContextResolver, keyEncryptManager);
    }

    @Override
    protected String buildKey(RepeatSubmitKey repeatSubmitKey) {
        return StrUtil.format(DEFAULT_KEY_SUFFIX,
                repeatSubmitKey.getServletUri(),
                repeatSubmitKey.getMethod(),
                repeatSubmitKey.getClientIp(),
                repeatSubmitKey.getClient(),
                repeatSubmitKey.getUserId(),
                repeatSubmitKey.getArgs());
    }
}
