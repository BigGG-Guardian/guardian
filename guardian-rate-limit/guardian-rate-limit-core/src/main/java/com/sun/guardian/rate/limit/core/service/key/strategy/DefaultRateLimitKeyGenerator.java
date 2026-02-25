package com.sun.guardian.rate.limit.core.service.key.strategy;

import com.sun.guardian.core.context.UserContext;
import com.sun.guardian.core.utils.template.TemplateUtil;
import com.sun.guardian.rate.limit.core.domain.key.RateLimitKey;

import static com.sun.guardian.rate.limit.core.constants.RateLimitKeyPrefixConstants.getSuffixByKeyScope;

/**
 * 限流键默认生成策略
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 17:29
 */
public class DefaultRateLimitKeyGenerator extends AbstractRateLimitKeyGenerator {

    /**
     * 构造默认限流键生成器
     */
    public DefaultRateLimitKeyGenerator(UserContext userContext) {
        super(userContext);
    }

    /** 拼接限流 Key */
    @Override
    protected String buildKey(RateLimitKey key) {
        return TemplateUtil.formatByBean(getSuffixByKeyScope(key.getKeyScope()), key);
    }
}
