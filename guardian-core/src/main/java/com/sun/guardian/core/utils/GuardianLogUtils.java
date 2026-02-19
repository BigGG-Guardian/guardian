package com.sun.guardian.core.utils;

import org.slf4j.Logger;

/**
 * 统一日志工具（debug=正常流程，warn=拦截事件）
 *
 * @author scj
 * @since 2026-02-09
 */
public class GuardianLogUtils {

    private final String moduleTag;
    private final String annotationName;

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

    /** 返回缓存结果 */
    public void cacheResultLog(boolean logEnable, Logger log, String uri, String key, String ip) {
        if (logEnable) {
            log.debug("{} 返回缓存结果 | URI: {} | Key: {} | IP: {}", moduleTag, uri, key, ip);
        }
    }
}
