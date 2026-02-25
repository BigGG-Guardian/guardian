package com.sun.guardian.core.utils.match;

/**
 * IP校验工具
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-24 21:11
 */
public class IpMatcher {

    private static final String IPV6_LOOPBACK_FULL = "0:0:0:0:0:0:0:1";
    private static final String IPV6_LOOPBACK_SHORT = "::1";

    /**
     * 判断 IP 是否匹配规则
     * <p>支持四种格式：</p>
     * <ul>
     *   <li>精确匹配：192.168.1.100</li>
     *   <li>通配符：192.168.1.*</li>
     *   <li>CIDR 网段：10.0.0.0/8、192.168.0.0/16</li>
     *   <li>IPv6 精确匹配：0:0:0:0:0:0:0:1、::1</li>
     * </ul>
     * <p>IPv6 客户端与 IPv4 规则（通配符/CIDR）不会匹配，直接返回 false。</p>
     */
    public static boolean matches(String clientIp, String rule) {
        if (clientIp == null || rule == null) {
            return false;
        }

        String normalizedIp = normalizeIp(clientIp);
        String normalizedRule = normalizeIp(rule.contains("/") ? rule.split("/")[0] : rule);

        if (rule.contains("/")) {
            if (isIpv6(normalizedIp) || isIpv6(normalizedRule)) {
                return false;
            }
            return matchesCidr(normalizedIp, rule);
        } else if (rule.contains("*")) {
            if (isIpv6(normalizedIp)) {
                return false;
            }
            return matchesWildcard(normalizedIp, rule);
        } else {
            return normalizedIp.equals(normalizeIp(rule));
        }
    }

    /**
     * 标准化 IP：IPv6 环回地址统一转为 127.0.0.1
     */
    private static String normalizeIp(String ip) {
        if (IPV6_LOOPBACK_FULL.equals(ip) || IPV6_LOOPBACK_SHORT.equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

    private static boolean isIpv6(String ip) {
        return ip.contains(":");
    }

    /**
     * CIDR 匹配（如 10.0.0.0/8）
     */
    private static boolean matchesCidr(String clientIp, String cidr) {
        String[] parts = cidr.split("/");
        String networkAddress = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        long clientIpLong = ipToLong(clientIp);
        long networkLong = ipToLong(networkAddress);
        long mask = -(1L << (32 - prefixLength));

        return (clientIpLong & mask) == (networkLong & mask);
    }

    /**
     * 通配符匹配（如 192.168.1.*）
     */
    private static boolean matchesWildcard(String clientIp, String rule) {
        String[] clientParts = clientIp.split("\\.");
        String[] ruleParts = rule.split("\\.");
        if (clientParts.length != 4 || ruleParts.length != 4) return false;
        for (int i = 0; i < 4; i++) {
            if (!"*".equals(ruleParts[i]) && !clientParts[i].equals(ruleParts[i])) {
                return false;
            }
        }
        return true;
    }

    private static long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        return (Long.parseLong(parts[0]) << 24)
                + (Long.parseLong(parts[1]) << 16)
                + (Long.parseLong(parts[2]) << 8)
                + Long.parseLong(parts[3]);
    }
}
