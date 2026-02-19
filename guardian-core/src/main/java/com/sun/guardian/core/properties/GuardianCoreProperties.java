package com.sun.guardian.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Guardian 全局共享配置（prefix = guardian）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-19
 */
@Data
@ConfigurationProperties(prefix = "guardian")
public class GuardianCoreProperties {

    /**
     * RepeatableRequestFilter 排序（值越小越先执行，默认 -100）
     * 全局只需配置一次，防重 / 幂等模块共用同一个 Filter 实例
     */
    private int repeatableFilterOrder = -100;
}
