package com.sun.guardian.trace.core.config;

import com.sun.guardian.core.service.base.BaseConfig;

/**
 * 请求链路配置属性接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-22 11:13
 */
public interface TraceConfig extends BaseConfig {

    /**
     * TraceId 请求头/响应头名称 Getter
     */
    String getHeaderName();
}
