package com.sun.guardian.auto.trim.starter.properties;

import com.sun.guardian.auto.trim.core.config.AutoTrimConfig;
import com.sun.guardian.core.service.base.BaseCharacterReplacement;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 请求参数自动trim配置参数
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 15:29
 */
@Data
@ConfigurationProperties(prefix = "guardian.auto-trim")
public class GuardianAutoTrimProperties implements AutoTrimConfig {
    /**
     * 总开关
     */
    private boolean enabled = true;
    /**
     * Filter 排序（值越小越先执行，默认 -10000，在业务 Filter 之前执行）
     */
    private int filterOrder = -10000;
    /**
     * 排除字段列表（表单参数 + JSON body 字段统一生效）
     */
    private Set<String> excludeFields = new LinkedHashSet<>();
    /**
     * 字符替换规则列表（先替换后 trim）
     */
    private List<CharacterReplacement> characterReplacements = new ArrayList<>();

    /**
     * 字符替换规则
     */
    @Data
    public static class CharacterReplacement implements BaseCharacterReplacement {
        /**
         * 待替换字符的转义表示，支持 \\r \\n \\t \\0 \\\\ \\uXXXX
         */
        private String from;
        /**
         * 替换为的目标字符串，默认空字符串（即删除）
         */
        private String to = "";
    }
}
