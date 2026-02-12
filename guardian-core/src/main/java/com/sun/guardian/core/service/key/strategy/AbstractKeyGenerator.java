package com.sun.guardian.core.service.key.strategy;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.sun.guardian.core.context.UserContextResolver;
import com.sun.guardian.core.domain.key.RepeatSubmitKey;
import com.sun.guardian.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.core.domain.token.RepeatSubmitToken;
import com.sun.guardian.core.service.encrypt.manager.KeyEncryptManager;
import com.sun.guardian.core.service.encrypt.strategy.AbstractKeyEncrypt;
import com.sun.guardian.core.service.key.KeyGenerator;
import com.sun.guardian.core.utils.ArgsUtil;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.sun.guardian.core.constants.KeyPrefixConstants.DEFAULT_KEY_PREFIX;

/**
 * 抽象防重键生成基类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 20:05
 */
@RequiredArgsConstructor
public abstract class AbstractKeyGenerator implements KeyGenerator {

    private final UserContextResolver userContextResolver;
    private final KeyEncryptManager keyEncryptManager;

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

    private RepeatSubmitKey buildRepeatSubmitKey(RepeatSubmitRule rule, HttpServletRequest request) {
        return new RepeatSubmitKey()
                .setUserId(resolveUserId(request))
                .setClient(rule.getClientType().key)
                .setClientIp(ServletUtil.getClientIP(request))
                .setMethod(request.getMethod())
                .setServletUri(request.getServletPath())
                .setArgs(ArgsUtil.toSortedJsonStr(request));
    }

    /**
     * 获取用户标识，未登录时使用 sessionId 兜底
     */
    private String resolveUserId(HttpServletRequest request) {
        String userId = userContextResolver.getUserId();
        if (StrUtil.isNotBlank(userId)) {
            return userId;
        }
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : ServletUtil.getClientIP(request);
    }

    protected abstract String buildKey(RepeatSubmitKey repeatSubmitKey);
}
