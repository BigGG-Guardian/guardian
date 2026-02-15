package com.sun.guardian.repeat.submit.core.utils;

import org.slf4j.Logger;

/**
 * Guardian-Repeat-Submit 日志打印工具类
 * <p>
 * 统一管理防重复提交拦截器的日志输出，所有日志方法均受 {@code logEnable} 开关控制。
 * <p>
 * 日志级别规范：
 * <ul>
 *   <li>{@code debug} — 正常流程（白名单放行、命中规则、正常放行），开发调试用</li>
 *   <li>{@code warn} — 拦截事件（重复提交被拦截），运维关注</li>
 * </ul>
 * <p>
 * 日志前缀统一为 {@code [Guardian-Repeat-Submit]} ，便于日志检索和过滤。
 *
 * @author scj
 * @since 2026-02-15
 */
public class LogUtils {

    private LogUtils() {
    }

    /**
     * 白名单放行日志
     * <p>
     * 请求 URI 命中排除规则（白名单），跳过所有防重检查直接放行。
     *
     * @param logEnable 日志开关
     * @param log       Logger 实例
     * @param uri       请求 URI
     * @param ip        客户端 IP
     */
    public static void excludeLog(boolean logEnable, Logger log, String uri, String ip) {
        if (logEnable) {
            log.debug("[Guardian-Repeat-Submit] 白名单放行 | URI: {} | IP: {}", uri, ip);
        }
    }

    /**
     * 命中 YAML URL 规则日志
     * <p>
     * 请求 URI 命中 YAML 配置的防重规则，即将执行防重检查。
     *
     * @param logEnable 日志开关
     * @param log       Logger 实例
     * @param uri       请求 URI
     * @param ip        客户端 IP
     */
    public static void hitYmlRuleLog(boolean logEnable, Logger log, String uri, String ip) {
        if (logEnable) {
            log.debug("[Guardian-Repeat-Submit] 命中 YAML 规则，开始防重检查 | URI: {} | IP: {}", uri, ip);
        }
    }

    /**
     * 命中注解规则日志
     * <p>
     * 请求对应的 Controller 方法标注了 {@code @RepeatSubmit} 注解，即将执行防重检查。
     *
     * @param logEnable 日志开关
     * @param log       Logger 实例
     * @param uri       请求 URI
     * @param ip        客户端 IP
     */
    public static void hitAnnotationRuleLog(boolean logEnable, Logger log, String uri, String ip) {
        if (logEnable) {
            log.debug("[Guardian-Repeat-Submit] 命中 @RepeatSubmit 注解，开始防重检查 | URI: {} | IP: {}", uri, ip);
        }
    }

    /**
     * 拦截重复请求日志（warn 级别）
     * <p>
     * 防重检查未通过，请求被判定为重复提交并拦截。
     * 此日志为 warn 级别，生产环境默认可见，便于运维监控。
     *
     * @param logEnable 日志开关
     * @param log       Logger 实例
     * @param uri       请求 URI
     * @param key       防重 Key
     * @param ip        客户端 IP
     */
    public static void repeatSubmitLog(boolean logEnable, Logger log, String uri, String key, String ip) {
        if (logEnable) {
            log.warn("[Guardian-Repeat-Submit] 拦截重复请求 | URI: {} | Key: {} | IP: {}", uri, key, ip);
        }
    }

    /**
     * 正常放行日志
     * <p>
     * 防重检查通过，请求被正常放行，防重 Key 已写入存储。
     *
     * @param logEnable 日志开关
     * @param log       Logger 实例
     * @param uri       请求 URI
     * @param key       防重 Key
     * @param ip        客户端 IP
     */
    public static void passLog(boolean logEnable, Logger log, String uri, String key, String ip) {
        if (logEnable) {
            log.debug("[Guardian-Repeat-Submit] 防重检查通过，放行 | URI: {} | Key: {} | IP: {}", uri, key, ip);
        }
    }
}
