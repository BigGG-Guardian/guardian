package com.sun.guardian.repeat.submit.core.service.key.strategy;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.sun.guardian.core.context.UserContext;
import com.sun.guardian.core.utils.ArgsUtil;
import com.sun.guardian.core.utils.UserContextUtils;
import com.sun.guardian.repeat.submit.core.domain.key.RepeatSubmitKey;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.repeat.submit.core.domain.token.RepeatSubmitToken;
import com.sun.guardian.repeat.submit.core.service.encrypt.manager.KeyEncryptManager;
import com.sun.guardian.repeat.submit.core.service.encrypt.strategy.AbstractKeyEncrypt;
import com.sun.guardian.repeat.submit.core.service.key.KeyGenerator;

import javax.servlet.http.HttpServletRequest;

import static com.sun.guardian.repeat.submit.core.constants.KeyPrefixConstants.DEFAULT_KEY_PREFIX;

/**
 * 防重键生成基类（模板方法）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 20:05
 */
public abstract class AbstractKeyGenerator implements KeyGenerator {

    private final UserContextUtils userContextUtils;
    private final KeyEncryptManager keyEncryptManager;

    protected AbstractKeyGenerator(UserContext userContext, KeyEncryptManager keyEncryptManager) {
        this.userContextUtils = new UserContextUtils(userContext);
        this.keyEncryptManager = keyEncryptManager;
    }

    @Override
    public RepeatSubmitToken generate(RepeatSubmitRule rule, HttpServletRequest request) {
        RepeatSubmitKey repeatSubmitKey = buildRepeatSubmitKey(rule, request);
        String key = buildKey(repeatSubmitKey);

        AbstractKeyEncrypt keyEncrypt = keyEncryptManager.getKeyEncrypt();
        String finishKey = StrUtil.format(DEFAULT_KEY_PREFIX, keyEncrypt.encrypt(key));

        return new RepeatSubmitToken()
                .setKey(finishKey)
                .setTimeout(rule.getInterval())
                .setTimeoutUnit(rule.getTimeUnit())
                .setCreateTime(DateUtil.date().getTime());
    }

    /** 组装防重键数据 */
    private RepeatSubmitKey buildRepeatSubmitKey(RepeatSubmitRule rule, HttpServletRequest request) {
        return new RepeatSubmitKey()
                .setUserId(userContextUtils.resolveUserId(request))
                .setKeyScope(rule.getKeyScope().key)
                .setClient(rule.getClientType().key)
                .setClientIp(ServletUtil.getClientIP(request))
                .setMethod(request.getMethod())
                .setServletUri(request.getServletPath())
                .setArgs(ArgsUtil.toSortedJsonStr(request));
    }

    /** 子类实现：拼接防重 Key */
    protected abstract String buildKey(RepeatSubmitKey repeatSubmitKey);
}
