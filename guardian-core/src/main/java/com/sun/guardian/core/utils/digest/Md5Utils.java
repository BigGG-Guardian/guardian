package com.sun.guardian.core.utils.digest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 摘要工具类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-25
 */
public class Md5Utils {

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private Md5Utils() {
    }

    /**
     * 计算字符串的 MD5 十六进制摘要
     */
    public static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = HEX_DIGITS[v >>> 4];
            hex[i * 2 + 1] = HEX_DIGITS[v & 0x0F];
        }
        return new String(hex);
    }
}
