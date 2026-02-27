package com.sun.guardian.anti.replay.core.config;

import com.sun.guardian.anti.replay.core.domain.rule.AntiReplayRule;
import com.sun.guardian.core.service.base.BaseConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 防重放攻击配置接口
 * <p>
 * 定义防重放模块所需的全部配置项，由 {@code GuardianAntiReplayProperties} 实现。
 * 拦截器/过滤器通过此接口引用配置，支持配置中心动态刷新。
 *
 * @author scj
 * @since 2026-02-27
 */
public interface AntiReplayConfig extends BaseConfig {

    /**
     * 获取 timestamp 有效窗口值
     *
     * @return 时间戳与服务器时间的最大允许偏差值
     */
    long getMaxAge();

    /**
     * 获取 timestamp 有效窗口的时间单位
     *
     * @return 时间单位，默认 {@link TimeUnit#SECONDS}
     */
    TimeUnit getMaxAgeUnit();

    /**
     * 获取 nonce 存活时间值
     * <p>
     * nonce TTL 必须大于等于 maxAge（转为同一单位后比较），
     * 否则攻击者可在 nonce 过期后篡改 timestamp 重放请求。
     *
     * @return nonce 在存储中的保留时间
     */
    long getNonceTtl();

    /**
     * 获取 nonce 存活时间的时间单位
     *
     * @return 时间单位，默认 {@link TimeUnit#SECONDS}
     */
    TimeUnit getNonceTtlUnit();

    /**
     * 获取时间戳请求头名称
     *
     * @return 请求头名称，默认 {@code X-Timestamp}
     */
    String getTimestampHeader();

    /**
     * 获取 Nonce 请求头名称
     *
     * @return 请求头名称，默认 {@code X-Nonce}
     */
    String getNonceHeader();

    /**
     * 获取缺少时间戳时的拒绝提示信息（支持 i18n Key）
     */
    String getMissingTimestampMessage();

    /**
     * 获取缺少 Nonce 时的拒绝提示信息（支持 i18n Key）
     */
    String getMissingNonceMessage();

    /**
     * 获取请求过期时的拒绝提示信息（支持 i18n Key）
     */
    String getExpiredMessage();

    /**
     * 获取重放攻击时的拒绝提示信息（支持 i18n Key）
     */
    String getReplayMessage();

    /**
     * 获取需要防重放保护的 URL 规则列表
     * <p>
     * 仅匹配的 URL 才会进行防重放校验；列表为空时对所有接口生效（需排除 exclude-urls）。
     *
     * @return URL 规则列表
     */
    List<AntiReplayRule> getUrls();
}
