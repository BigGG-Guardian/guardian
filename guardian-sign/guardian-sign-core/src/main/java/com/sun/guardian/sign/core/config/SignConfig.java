package com.sun.guardian.sign.core.config;

import com.sun.guardian.core.service.base.BaseConfig;
import com.sun.guardian.sign.core.domain.rule.SignRule;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 参数签名配置接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-02 20:40
 */
public interface SignConfig extends BaseConfig {

    /**
     * 是否返回值签名 Getter
     */
    boolean isResultSign();

    /**
     * 密钥 Getter
     */
    String getSecretKey();

    /**
     * 签名请求头名称 Getter
     */
    String getSignHeader();

    /**
     * 时间戳请求头名称 Getter
     */
    String getTimestampHeader();

    /**
     * 时间戳过期时间 Getter
     */
    long getMaxAge();

    /**
     * 时间戳过期时间单位 Getter
     */
    TimeUnit getMaxAgeUnit();

    /**
     * 缺少时间戳时的拒绝提示信息 Getter
     */
    String getMissingTimestampMessage();

    /**
     * 缺少 Sign 时的拒绝提示信息 Getter
     */
    String getMissingSignMessage();

    /**
     * 请求过期时的拒绝提示信息 Getter
     */
    String getExpiredMessage();

    /**
     * 需要参数签名的 URL 规则列表 Getter
     */
    List<SignRule> getUrls();

    /**
     * 返回值签名 Advice 排序 Getter
     */
    int getResultAdviceOrder();
}
