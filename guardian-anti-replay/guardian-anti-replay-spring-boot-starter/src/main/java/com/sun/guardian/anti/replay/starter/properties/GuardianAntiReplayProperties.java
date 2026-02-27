package com.sun.guardian.anti.replay.starter.properties;

import com.sun.guardian.anti.replay.core.config.AntiReplayConfig;
import com.sun.guardian.anti.replay.core.domain.rule.AntiReplayRule;
import com.sun.guardian.core.properties.BaseGuardianProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 防重放攻击配置属性
 * <p>
 * 对应 YAML 前缀 {@code guardian.anti-replay}，实现 {@link AntiReplayConfig} 接口。
 *
 * @author scj
 * @since 2026-02-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "guardian.anti-replay")
public class GuardianAntiReplayProperties extends BaseGuardianProperties implements AntiReplayConfig {

    /**
     * 总开关（默认 true，需显式关闭）
     */
    private boolean enabled = true;

    /**
     * timestamp 有效窗口（默认 60）
     */
    private long maxAge = 60;

    /**
     * timestamp 有效窗口时间单位（默认秒）
     */
    private TimeUnit maxAgeUnit = TimeUnit.SECONDS;

    /**
     * nonce 存活时间（默认 86400，即 24 小时）
     * <p>
     * 必须大于等于 maxAge（转为同一单位后比较），否则攻击者可在 nonce 过期后篡改 timestamp 重放请求。
     * 搭配 guardian-sign 签名模块使用时，nonceTtl 可与 maxAge 相同。
     */
    private long nonceTtl = 86400;

    /**
     * nonce 存活时间单位（默认秒）
     */
    private TimeUnit nonceTtlUnit = TimeUnit.SECONDS;

    /**
     * 时间戳请求头名称（默认 X-Timestamp）
     */
    private String timestampHeader = "X-Timestamp";

    /**
     * Nonce 请求头名称（默认 X-Nonce）
     */
    private String nonceHeader = "X-Nonce";

    /**
     * Filter 排序（默认 -14000，在 IP 黑白名单之后、参数 Trim 之前）
     */
    private int filterOrder = -14000;

    /**
     * 缺少时间戳时的拒绝提示信息（支持 i18n Key）
     */
    private String missingTimestampMessage = "缺少时间戳";

    /**
     * 缺少 Nonce 时的拒绝提示信息（支持 i18n Key）
     */
    private String missingNonceMessage = "缺少请求标识";

    /**
     * 请求过期时的拒绝提示信息（支持 i18n Key）
     */
    private String expiredMessage = "请求已过期";

    /**
     * 重放攻击时的拒绝提示信息（支持 i18n Key）
     */
    private String replayMessage = "重复请求";

    /**
     * 需要防重放保护的 URL 规则列表（AntPath 格式）
     */
    private List<AntiReplayRule> urls = new ArrayList<>();

    /**
     * 排除规则（白名单，优先级最高，命中直接放行）
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * 校验参数合法性，不合法时抛出 {@link IllegalArgumentException}
     */
    public void validate() {
        if (maxAge <= 0) {
            throw new IllegalArgumentException("[Guardian-Anti-Replay] maxAge 必须大于 0，当前值：" + maxAge);
        }
        if (nonceTtl <= 0) {
            throw new IllegalArgumentException("[Guardian-Anti-Replay] nonceTtl 必须大于 0，当前值：" + nonceTtl);
        }
        long maxAgeMillis = maxAgeUnit.toMillis(maxAge);
        long nonceTtlMillis = nonceTtlUnit.toMillis(nonceTtl);
        if (nonceTtlMillis < maxAgeMillis) {
            throw new IllegalArgumentException(
                    "[Guardian-Anti-Replay] nonceTtl（" + nonceTtl + " " + nonceTtlUnit +
                    "）必须大于等于 maxAge（" + maxAge + " " + maxAgeUnit + "）");
        }
    }
}
