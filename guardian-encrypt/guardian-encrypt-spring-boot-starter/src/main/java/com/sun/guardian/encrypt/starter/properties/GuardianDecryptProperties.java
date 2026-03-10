package com.sun.guardian.encrypt.starter.properties;

import com.sun.guardian.core.enums.response.ResponseMode;
import com.sun.guardian.encrypt.core.config.decrypt.DecryptConfig;
import com.sun.guardian.encrypt.core.domain.properties.DataProperties;
import com.sun.guardian.encrypt.core.domain.properties.decrypt.KeyDecryptProperties;
import com.sun.guardian.encrypt.core.domain.rule.EncryptRule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求解密配置参数
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-05 18:28
 */
@Data
@ConfigurationProperties(prefix = "guardian.decrypt")
public class GuardianDecryptProperties implements DecryptConfig {

    /**
     * 总开关
     */
    private boolean enabled = true;

    /**
     * Filter 排序（值越小越先执行，默认 -40000，确保最先执行以覆盖全链路）
     */
    private int filterOrder = -40000;

    /**
     * 密钥解密参数
     */
    private KeyDecryptProperties key = new KeyDecryptProperties();

    /**
     * 数据解密参数
     */
    private DataProperties data = new DataProperties();

    /**
     * 缺少数据密钥请求头 提示信息（支持 i18n Key）
     */
    private String missingDataKeyHeaderMessage = "缺少数据密钥请求头";

    /**
     * 需要解密的 URL 规则列表（AntPath 格式）
     */
    private List<EncryptRule> urls = new ArrayList<>();

    /**
     * 排除规则（白名单，优先级最高，命中直接放行）
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * 响应模式：exception / json
     */
    private ResponseMode responseMode = ResponseMode.EXCEPTION;

    /**
     * 是否打印拦截日志（默认 false）
     */
    private boolean logEnabled = false;

}
