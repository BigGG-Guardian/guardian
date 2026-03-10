package com.sun.guardian.encrypt.starter.properties;

import com.sun.guardian.encrypt.core.config.encrypt.EncryptConfig;
import com.sun.guardian.encrypt.core.domain.properties.DataProperties;
import com.sun.guardian.encrypt.core.domain.properties.encrypt.KeyEncryptProperties;
import com.sun.guardian.encrypt.core.domain.rule.EncryptRule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求加密配置参数
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 18:28
 */
@Data
@ConfigurationProperties(prefix = "guardian.encrypt")
public class GuardianEncryptProperties implements EncryptConfig {

    /**
     * 总开关
     */
    private boolean enabled = true;

    /**
     * 返回值加密 Advice 排序（值越小越先执行，默认 200）
     * 用于控制多个 ResponseBodyAdvice 的执行顺序
     * 推荐顺序：幂等缓存(200) -> 签名(100) -> 加密(300)
     */
    private int resultAdviceOrder = 300;

    /**
     * 密钥加密参数
     */
    private KeyEncryptProperties key = new KeyEncryptProperties();

    /**
     * 数据加密参数
     */
    private DataProperties data = new DataProperties();

    /**
     * 需要加密的 URL 规则列表（AntPath 格式）
     */
    private List<EncryptRule> urls = new ArrayList<>();

    /**
     * 排除规则（白名单，优先级最高，命中直接放行）
     */
    private List<String> excludeUrls = new ArrayList<>();
}
