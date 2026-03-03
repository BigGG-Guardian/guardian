package com.sun.guardian.sign.core.service.sign;

import com.sun.guardian.sign.core.enums.algorithm.SignAlgorithm;

import java.util.SortedMap;

/**
 * 参数签名接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-02 21:20
 */
public interface SignService {

    /**
     * 计算签名
     *
     * @param params    请求参数（已按 key 排序）
     * @param timestamp 时间戳
     * @param secretKey 密钥
     * @param algorithm 签名算法
     * @return 签名值
     */
    String sign(SortedMap<String, String> params, String timestamp, String secretKey, SignAlgorithm algorithm);

}
