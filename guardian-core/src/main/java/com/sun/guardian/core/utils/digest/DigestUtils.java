package com.sun.guardian.core.utils.digest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.util.Base64;

/**
 * 算法 摘要工具类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-25
 */
public class DigestUtils {

    private DigestUtils() {
    }

    /**
     * 计算字符串的 base64
     */
    public static String base64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算字符串的 MD5 十六进制摘要
     */
    public static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return ByteUtils.bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5算法不可用", e);
        }
    }

    /**
     * 计算字符串的 SHA256 十六进制摘要
     */
    public static String sha256Hex(String input) {
        try {
            MessageDigest SHA256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = SHA256.digest(input.getBytes(StandardCharsets.UTF_8));
            return ByteUtils.bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256算法不可用", e);
        }
    }

    /**
     * 计算字符串的 HMAC-SHA256 十六进制摘要
     */
    public static String HmacSha256Hex(String input, String secretKey) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(keySpec);
            byte[] digest = hmacSHA256.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return ByteUtils.bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("HmacSHA256算法不可用", e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("无效的密钥", e);
        }
    }
}
