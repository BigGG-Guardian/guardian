package com.sun.guardian.repeat.submit.core.service.key.strategy;

import com.sun.guardian.core.context.UserContext;
import com.sun.guardian.repeat.submit.core.domain.key.RepeatSubmitKey;
import com.sun.guardian.repeat.submit.core.service.encrypt.manager.KeyEncryptManager;
import com.sun.guardian.core.utils.TemplateUtil;

import static com.sun.guardian.repeat.submit.core.constants.KeyPrefixConstants.getSuffixByKeyScope;

/**
 * 防重键默认生成策略
 * <p>
 * 根据 keyScope 维度选取对应模板，按字段名填充生成最终 Key
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 20:01
 */
public class DefaultKeyGenerator extends AbstractKeyGenerator {

    public DefaultKeyGenerator(UserContext userContext, KeyEncryptManager keyEncryptManager) {
        super(userContext, keyEncryptManager);
    }

    @Override
    protected String buildKey(RepeatSubmitKey key) {
        return TemplateUtil.formatByBean(getSuffixByKeyScope(key.getKeyScope()), key);
    }
}
