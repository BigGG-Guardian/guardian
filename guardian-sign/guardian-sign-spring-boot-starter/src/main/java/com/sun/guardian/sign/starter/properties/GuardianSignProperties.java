package com.sun.guardian.sign.starter.properties;

import com.sun.guardian.core.enums.response.ResponseMode;
import com.sun.guardian.sign.core.config.SignConfig;
import com.sun.guardian.sign.core.domain.rule.SignRule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author scj
 * @version java version 1.8
 * @since 2026-03-02 20:41
 */
@Data
@ConfigurationProperties(prefix = "guardian.sign")
public class GuardianSignProperties implements SignConfig {

    /**
     * 总开关（默认 true，需显式关闭）
     */
    private boolean enabled = true;

    /**
     * 返回值签名（默认 false）
     */
    private boolean resultSign = false;

    /**
     * 密钥
     */
    private String secretKey = "";

    /**
     * 签名请求头名称
     */
    private String signHeader = "X-Sign";

    /**
     * 时间戳请求头名称
     */
    private String timestampHeader = "X-Sign-Timestamp";

    /**
     * 时间戳过期时间,0表示不校验时间戳
     */
    private long maxAge = 60;

    /**
     * 时间戳过期时间单位
     */
    private TimeUnit maxAgeUnit = TimeUnit.SECONDS;

    /**
     * 缺少时间戳时的拒绝提示信息（支持 i18n Key）
     */
    private String missingTimestampMessage = "缺少时间戳";

    /**
     * 缺少 Sign 时的拒绝提示信息（支持 i18n Key）
     */
    private String missingSignMessage = "缺少参数签名";

    /**
     * 请求过期时的拒绝提示信息（支持 i18n Key）
     */
    private String expiredMessage = "请求已过期";

    /**
     * 需要参数签名的 URL 规则列表（AntPath 格式）
     */
    private List<SignRule> urls = new ArrayList<>();

    /**
     * 拦截器排序（值越小越先执行）
     */
    private int interceptorOrder = 4000;

    /**
     * 返回值签名 Advice 排序（值越小越先执行，默认 100）
     * 用于控制多个 ResponseBodyAdvice 的执行顺序
     * 推荐顺序：幂等缓存(200) -> 签名(100) -> 加密(300)
     */
    private int resultAdviceOrder = 100;

    /**
     * 响应模式：exception / json
     */
    private ResponseMode responseMode = ResponseMode.EXCEPTION;

    /**
     * 是否打印拦截日志（默认 false）
     */
    private boolean logEnabled = false;

    /**
     * 校验参数合法性，不合法时抛出 {@link IllegalArgumentException}
     */
    public void validate() {
        if (maxAge < 0) {
            throw new IllegalArgumentException("[Guardian-Sign] maxAge 必须大于等于 0，当前值：" + maxAge);
        }
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("[Guardian-Sign] secretKey 不能为空");
        }
    }
}
