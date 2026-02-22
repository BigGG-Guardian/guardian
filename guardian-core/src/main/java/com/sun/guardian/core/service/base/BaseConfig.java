package com.sun.guardian.core.service.base;

import com.sun.guardian.core.enums.response.ResponseMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置属性公共接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-22 10:15
 */
public interface BaseConfig {

    /**
     * 排除规则（白名单） Getter
     */
    default List<String> getExcludeUrls() {
        return new ArrayList<>();
    }

    /**
     * 是否打印拦截日志 Getter
     */
    default boolean isLogEnabled() {
        return false;
    }

    /**
     * 响应模式 Getter
     */
    default ResponseMode getResponseMode() {
        return ResponseMode.EXCEPTION;
    }

    /**
     * 字符替换规则列表（先替换后 trim）Getter
     */
    default List<? extends BaseCharacterReplacement> getCharacterReplacements() {
        return new ArrayList<>();
    }
}
