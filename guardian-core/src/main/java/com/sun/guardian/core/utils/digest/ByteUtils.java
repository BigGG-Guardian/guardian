package com.sun.guardian.core.utils.digest;

/**
 * 字节工具类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-02 21:27
 */
public class ByteUtils {

    private ByteUtils() {
    }

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    protected static String bytesToHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = HEX_DIGITS[v >>> 4];
            hex[i * 2 + 1] = HEX_DIGITS[v & 0x0F];
        }
        return new String(hex);
    }

}
