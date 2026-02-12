package com.sun.guardian.repeat.submit.core.storage;

import com.sun.guardian.repeat.submit.core.domain.token.RepeatSubmitToken;

/**
 * 防重提交存储接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09 19:43
 */
public interface RepeatSubmitStorage {

    /**
     * 尝试提交令牌
     *
     * @param token 防重令牌
     * @return true-允许提交 false-拒绝提交
     */
    boolean tryAcquire(RepeatSubmitToken token);

    /**
     * 手动释放令牌
     *
     * @param token 防重令牌
     */
    void release(RepeatSubmitToken token);
}
