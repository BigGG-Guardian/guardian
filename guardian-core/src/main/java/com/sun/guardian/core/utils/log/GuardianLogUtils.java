package com.sun.guardian.core.utils.log;

import org.slf4j.Logger;

/**
 * 统一日志工具（debug=正常流程，warn=拦截事件）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09
 */
public class GuardianLogUtils {

    private final String moduleTag;
    private final String annotationName;

    /**
     * 构造日志工具
     */
    public GuardianLogUtils(String moduleTag, String annotationName) {
        this.moduleTag = moduleTag;
        this.annotationName = annotationName;
    }

    /**
     * 白名单放行
     */
    public void excludeLog(boolean logEnable, Logger log, String uri, String ip) {
        if (logEnable) {
            log.debug("{} 白名单放行 | URI: {} | IP: {}", moduleTag, uri, ip);
        }
    }

    /**
     * 命中 YAML 规则
     */
    public void hitYmlRuleLog(boolean logEnable, Logger log, String uri, String ip) {
        if (logEnable) {
            log.debug("{} 命中 YAML 规则，开始检查 | URI: {} | IP: {}", moduleTag, uri, ip);
        }
    }

    /**
     * 命中注解规则
     */
    public void hitAnnotationRuleLog(boolean logEnable, Logger log, String uri, String ip) {
        if (logEnable) {
            log.debug("{} 命中 {} 注解，开始检查 | URI: {} | IP: {}", moduleTag, annotationName, uri, ip);
        }
    }

    /**
     * 请求被拦截（warn）
     */
    public void blockLog(boolean logEnable, Logger log, String uri, String key, String ip) {
        if (logEnable) {
            log.warn("{} 请求被拦截 | URI: {} | Key: {} | IP: {}", moduleTag, uri, key, ip);
        }
    }

    /**
     * 请求被拦截（warn）
     */
    public void blockLog(boolean logEnable, Logger log, String uri, String ip) {
        if (logEnable) {
            log.warn("{} 请求被拦截 | URI: {} | IP: {}", moduleTag, uri, ip);
        }
    }

    /**
     * 检查通过，放行
     */
    public void passLog(boolean logEnable, Logger log, String uri, String key, String ip) {
        if (logEnable) {
            log.debug("{} 检查通过，放行 | URI: {} | Key: {} | IP: {}", moduleTag, uri, key, ip);
        }
    }

    /**
     * 检查通过，放行
     */
    public void passLog(boolean logEnable, Logger log, String uri, String ip) {
        if (logEnable) {
            log.debug("{} 检查通过，放行 | URI: {} | IP: {}", moduleTag, uri, ip);
        }
    }

    /**
     * 返回缓存结果
     */
    public void cacheResultLog(boolean logEnable, Logger log, String uri, String key, String ip) {
        if (logEnable) {
            log.debug("{} 返回缓存结果 | URI: {} | Key: {} | IP: {}", moduleTag, uri, key, ip);
        }
    }

    /**
     * 慢接口检测日志
     */
    public void slowApiLog(Logger log, String method, String uri, long duration, long threshold) {
        log.warn("{} 慢接口检测 | {} {} | 耗时: {}ms | 阈值: {}ms", moduleTag, method, uri, duration, threshold);
    }

    /**
     * IP黑名单拦截
     */
    public void ipBlackBlockLog(boolean logEnable, Logger log, String ip, String uri) {
        if (logEnable) {
            log.warn("{} 黑名单拦截 | IP: {} | URI: {}", moduleTag, ip, uri);
        }
    }

    /**
     * IP白名单拦截
     */
    public void ipWhiteBlockLog(boolean logEnable, Logger log, String ip, String uri) {
        if (logEnable) {
            log.warn("{} 白名单拦截 | IP: {} | URI: {} | 不在白名单内", moduleTag, ip, uri);
        }
    }

    /**
     * 接口关闭拦截
     */
    public void disabledLog(boolean logEnable, Logger log, String uri, String ip) {
        if (logEnable) {
            log.debug("{} 接口关闭拦截 | URI: {} | IP: {}", moduleTag, uri, ip);
        }
    }
}
