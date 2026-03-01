package com.sun.guardian.core.utils.url;

import org.springframework.util.StringUtils;

/**
 * URL工具类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-28 22:13
 */
public class UrlUtils {

    /**
     * 判断URL是否/开头，否则添加/，并将路径中的,替换为/
     */
    public static String addSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        return url.replace(",", "/");
    }
}
