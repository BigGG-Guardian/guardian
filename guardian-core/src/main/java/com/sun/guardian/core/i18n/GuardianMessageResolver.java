package com.sun.guardian.core.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 消息解析工具
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-24 16:22
 */
public class GuardianMessageResolver {

    private final MessageSource messageSource;

    /**
     * @param messageSource 可为 null（未配置国际化时直接返回原始 message）
     */
    public GuardianMessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 解析消息：尝试从 MessageSource 解析，解析不到则原样返回
     */
    public String resolve(String message) {
        if (messageSource == null || message == null || message.trim().isEmpty()) {
            return message;
        }
        Locale locale = LocaleContextHolder.getLocale();

        return messageSource.getMessage(message, null, message, locale);
    }
}
