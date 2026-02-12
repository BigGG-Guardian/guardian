package com.sun.guardian.repeat.submit.core.service.key.strategy;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.sun.guardian.repeat.submit.core.context.UserContextResolver;
import com.sun.guardian.repeat.submit.core.domain.key.RepeatSubmitKey;
import com.sun.guardian.repeat.submit.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.repeat.submit.core.domain.token.RepeatSubmitToken;
import com.sun.guardian.repeat.submit.core.service.encrypt.manager.KeyEncryptManager;
import com.sun.guardian.repeat.submit.core.service.encrypt.strategy.AbstractKeyEncrypt;
import com.sun.guardian.repeat.submit.core.service.key.KeyGenerator;
import com.sun.guardian.core.utils.ArgsUtil;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.sun.guardian.repeat.submit.core.constants.KeyPrefixConstants.DEFAULT_KEY_PREFIX;

/**
 * 抽象防重键生成基类（模板方法模式）
 * <p>
 * 定义防重 Key 的生成流程：
 * <ol>
 *   <li>从请求和规则中提取各维度信息，组装 {@link RepeatSubmitKey}</li>
 *   <li>调用子类 {@link #buildKey(RepeatSubmitKey)} 拼接 Key 内容</li>
 *   <li>通过 {@link KeyEncryptManager} 加密后包装为最终 Key</li>
 *   <li>封装为 {@link RepeatSubmitToken} 返回</li>
 * </ol>
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

    /**
     * 从请求和规则中提取各维度信息，组装防重键数据载体
     *
     * @param rule    当前生效的防重规则（来自注解或 YAML）
     * @param request 当前 HTTP 请求
     * @return 包含所有维度信息的 {@link RepeatSubmitKey}
     */
    private RepeatSubmitKey buildRepeatSubmitKey(RepeatSubmitRule rule, HttpServletRequest request) {
        return new RepeatSubmitKey()
                .setUserId(resolveUserId(request))
                .setKeyScope(rule.getKeyScope().key)
                .setClient(rule.getClientType().key)
                .setClientIp(ServletUtil.getClientIP(request))
                .setMethod(request.getMethod())
                .setServletUri(request.getServletPath())
                .setArgs(ArgsUtil.toSortedJsonStr(request));
    }

    /**
     * 解析当前用户标识，三级降级策略：
     * <ol>
     *   <li>已登录 → {@link UserContextResolver#getUserId()}</li>
     *   <li>未登录但有 Session → sessionId</li>
     *   <li>无 Session → 客户端 IP</li>
     * </ol>
     *
     * @param request 当前 HTTP 请求
     * @return 用户标识，永不为 null
     */
    private String resolveUserId(HttpServletRequest request) {
        String userId = userContextResolver.getUserId();
        if (StrUtil.isNotBlank(userId)) {
            return userId;
        }
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : ServletUtil.getClientIP(request);
    }

    /**
     * 由子类实现，根据 {@link RepeatSubmitKey} 拼接防重 Key 内容
     *
     * @param repeatSubmitKey 包含所有维度信息的数据载体
     * @return 拼接后的 Key 字符串（未加密）
     */
    protected abstract String buildKey(RepeatSubmitKey repeatSubmitKey);
}
