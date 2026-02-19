package com.sun.guardian.core.properties;

import com.sun.guardian.core.enums.response.ResponseMode;
import com.sun.guardian.core.enums.storage.StorageType;
import lombok.Data;

/**
 * Guardian 模块公共配置基类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-19
 */
@Data
public abstract class BaseGuardianProperties {

    /**
     * 存储类型：redis / local
     */
    private StorageType storage = StorageType.REDIS;

    /**
     * 响应模式：exception / json
     */
    private ResponseMode responseMode = ResponseMode.EXCEPTION;

    /**
     * 是否打印拦截日志（默认 false）
     */
    private boolean logEnabled = false;

    /**
     * 拦截器排序（值越小越先执行）
     */
    private int interceptorOrder;
}
