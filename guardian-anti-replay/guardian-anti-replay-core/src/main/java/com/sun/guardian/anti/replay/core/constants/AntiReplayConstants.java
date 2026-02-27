package com.sun.guardian.anti.replay.core.constants;

/**
 * 防重放攻击模块常量
 *
 * @author scj
 * @since 2026-02-27
 */
public interface AntiReplayConstants {

    /**
     * Nonce Redis Key 前缀，{@code %s} 占位为具体的 nonce 值
     */
    String KEY_PREFIX = "guardian:anti-replay:%s";
}
