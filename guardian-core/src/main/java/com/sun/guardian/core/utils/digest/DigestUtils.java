package com.sun.guardian.core.utils.digest;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.math.ec.ECPoint;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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

    /**
     * 计算字符串的 SM3 十六进制摘要
     */
    public static String sm3Hex(String input) {
        SM3Digest digest = new SM3Digest();
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        digest.update(data, 0, data.length);

        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return ByteUtils.bytesToHex(result);
    }

    /**
     * 计算字符串的 SM3 Base64 摘要
     */
    public static String sm3Base64(String input) {
        SM3Digest digest = new SM3Digest();
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        digest.update(data, 0, data.length);

        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return Base64Utils.encodeToString(result);
    }

    /**
     * RSA解密
     */
    public static String rsaDecrypt(String encryptedData, String privateKey) throws Exception {
        byte[] privateKeyBytes = Base64Utils.decodeFromString(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKeyObj = keyFactory.generatePrivate(keySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        cipher.init(Cipher.DECRYPT_MODE, privateKeyObj);

        byte[] data = Base64Utils.decodeFromString(encryptedData);

        return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
    }

    /**
     * RSA加密
     */
    public static String rsaEncrypt(String data, String publicKey) throws Exception {
        byte[] publicKeyBytes = Base64Utils.decodeFromString(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKeyObj = keyFactory.generatePublic(keySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        cipher.init(Cipher.ENCRYPT_MODE, publicKeyObj);

        byte[] resultBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return Base64Utils.encodeToString(resultBytes);
    }

    /**
     * AES解密
     */
    public static String aesDecrypt(String encryptedData, String key) throws Exception {
        byte[] keyBytes = Base64Utils.decodeFromString(key);
        SecretKey keyObj = new SecretKeySpec(keyBytes, "AES");
        byte[] combined = Base64Utils.decodeFromString(encryptedData);

        byte[] iv = new byte[12];
        byte[] encrypted = new byte[combined.length - 12];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keyObj, new GCMParameterSpec(128, iv));

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * AES加密
     */
    public static String aesEncrypt(String data, String key) throws Exception {
        byte[] keyBytes = Base64Utils.decodeFromString(key);
        SecretKey keyObj = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keyObj, new GCMParameterSpec(128, iv));

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64Utils.encodeToString(combined);
    }

    /**
     * SM4 解密（CBC 模式，PKCS7 填充）
     *
     * @param encryptedData 密文（Base64 格式，IV + 密文）
     * @param key           密钥（Base64 格式，16字节）
     */
    public static String sm4Decrypt(String encryptedData, String key) throws Exception {
        byte[] keyBytes = Base64Utils.decodeFromString(key);
        byte[] combined = Base64Utils.decodeFromString(encryptedData);

        byte[] iv = new byte[16];
        byte[] encrypted = new byte[combined.length - 16];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

        SM4Engine engine = new SM4Engine();
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine), new PKCS7Padding());
        cipher.init(false, new ParametersWithIV(new KeyParameter(keyBytes), iv));

        byte[] decrypted = new byte[cipher.getOutputSize(encrypted.length)];
        int len = cipher.processBytes(encrypted, 0, encrypted.length, decrypted, 0);
        len += cipher.doFinal(decrypted, len);

        byte[] result = new byte[len];
        System.arraycopy(decrypted, 0, result, 0, len);

        return hexToString(new String(result, StandardCharsets.UTF_8));
    }

    /**
     * SM4 加密（CBC 模式，PKCS7 填充）
     *
     * @param data 明文
     * @param key  密钥（Base64 格式，16字节）
     * @return 密文（Base64 格式，IV + 密文）
     */
    public static String sm4Encrypt(String data, String key) throws Exception {
        byte[] keyBytes = Base64Utils.decodeFromString(key);
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        SM4Engine engine = new SM4Engine();
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine), new PKCS7Padding());
        cipher.init(true, new ParametersWithIV(new KeyParameter(keyBytes), iv));

        String hexData = stringToHex(data);
        byte[] dataBytes = hexData.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = new byte[cipher.getOutputSize(dataBytes.length)];
        int len = cipher.processBytes(dataBytes, 0, dataBytes.length, encrypted, 0);
        cipher.doFinal(encrypted, len);

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64Utils.encodeToString(combined);
    }


    /**
     * SM2 解密
     *
     * @param encryptedData 密文（Base64 格式）
     * @param privateKey    私钥（Base64 格式）
     */
    public static String sm2Decrypt(String encryptedData, String privateKey) throws Exception {
        X9ECParameters sm2Params = GMNamedCurves.getByName("sm2p256v1");
        ECDomainParameters domainParameters = new ECDomainParameters(
                sm2Params.getCurve(), sm2Params.getG(), sm2Params.getN());

        BigInteger privateKeyValue = new BigInteger(1, Base64Utils.decodeFromString(privateKey));
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyValue, domainParameters);

        SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        engine.init(false, privateKeyParameters);

        byte[] data = Base64Utils.decodeFromString(encryptedData);
        byte[] result = engine.processBlock(data, 0, data.length);
        return hexToString(new String(result, StandardCharsets.UTF_8));
    }

    /**
     * SM2 加密
     *
     * @param data      明文
     * @param publicKey 公钥（Base64 格式，04 + X + Y）
     */
    public static String sm2Encrypt(String data, String publicKey) throws Exception {
        X9ECParameters sm2Params = GMNamedCurves.getByName("sm2p256v1");
        ECDomainParameters domainParameters = new ECDomainParameters(
                sm2Params.getCurve(), sm2Params.getG(), sm2Params.getN());

        ECPoint publicKeyPoint = sm2Params.getCurve().decodePoint(Base64Utils.decodeFromString(publicKey));
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(publicKeyPoint, domainParameters);

        SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        engine.init(true, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));

        String hexData = stringToHex(data);
        byte[] dataBytes = hexData.getBytes(StandardCharsets.UTF_8);
        byte[] result = engine.processBlock(dataBytes, 0, dataBytes.length);
        return Base64Utils.encodeToString(result);
    }

    /**
     * 字符串转换为 Hex 格式
     */
    private static String stringToHex(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }

    /**
     * Hex 字符串转换为原始字符串
     */
    private static String hexToString(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
