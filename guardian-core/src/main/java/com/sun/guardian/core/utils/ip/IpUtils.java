package com.sun.guardian.core.utils.ip;

import javax.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 获取工具类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-25
 */
public class IpUtils {

    private static final String UNKNOWN = "unknown";

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private IpUtils() {
    }

    /**
     * 获取客户端真实 IP，依次从代理头中读取，最终 fallback 到 remoteAddr
     */
    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip.trim();
            }
        }
        return request.getRemoteAddr();
    }
}
