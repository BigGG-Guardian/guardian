package com.sun.guardian.repeat.submit.core.domain.key;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 防重键数据载体
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 20:25
 */
@Accessors(chain = true)
@Data
public class RepeatSubmitKey {

    private String userId;
    private String keyScope;
    private String client;
    private String clientIp;
    private String method;
    private String servletUri;
    private String args;
}
