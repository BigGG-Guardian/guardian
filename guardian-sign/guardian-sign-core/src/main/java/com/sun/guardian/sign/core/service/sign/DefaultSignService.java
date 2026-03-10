package com.sun.guardian.sign.core.service.sign;

import com.sun.guardian.core.utils.digest.DigestUtils;
import com.sun.guardian.sign.core.enums.algorithm.SignAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * 默认参数签名接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-02 21:20
 */
public class DefaultSignService implements SignService {
    private static final Logger log = LoggerFactory.getLogger(DefaultSignService.class);

    @Override
    public String sign(SortedMap<String, String> params, String timestamp, String secretKey, SignAlgorithm algorithm) {
        String paramStr = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        String content = paramStr + "&timestamp=" + timestamp + "&key=" + secretKey;

        switch (algorithm) {
            case MD5:
                return DigestUtils.md5Hex(content);
            case SHA256:
                return DigestUtils.sha256Hex(content);
            case HMAC_SHA256:
                return DigestUtils.HmacSha256Hex(content, secretKey);
            case SM3:
                return DigestUtils.sm3Hex(content);
            default:
                return DigestUtils.base64(content);
        }
    }
}
