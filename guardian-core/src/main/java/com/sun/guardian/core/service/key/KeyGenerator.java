package com.sun.guardian.core.service.key;

import com.sun.guardian.core.domain.rule.RepeatSubmitRule;
import com.sun.guardian.core.domain.token.RepeatSubmitToken;

import javax.servlet.http.HttpServletRequest;

/**
 * 防重键生成器接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 19:55
 */
public interface KeyGenerator {

    /**
     * 生成防重令牌
     *
     * @param rule    防重规则（来自注解或 yml 配置）
     * @param request 当前请求
     * @return 防重令牌
     */
    RepeatSubmitToken generate(RepeatSubmitRule rule, HttpServletRequest request);
}
