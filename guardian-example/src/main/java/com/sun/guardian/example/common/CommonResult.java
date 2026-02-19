package com.sun.guardian.example.common;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 通用返回结果封装
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 11:26
 */
@Accessors(chain = true)
@Data
public class CommonResult<T> {

    /**
     * 状态码：200 成功，500 失败
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 时间戳（毫秒）
     */
    private long timestamp;

    /**
     * 响应
     */
    public static <T> CommonResult<T> result(Integer code, T data, String message) {
        return new CommonResult<T>()
                .setCode(code)
                .setData(data)
                .setMessage(message)
                .setTimestamp(DateUtil.current());
    }

    /**
     * 成功响应
     */
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>()
                .setCode(200)
                .setData(data)
                .setMessage("success")
                .setTimestamp(DateUtil.current());
    }

    /**
     * 失败响应
     */
    public static <T> CommonResult<T> error(String message) {
        return new CommonResult<T>()
                .setCode(500)
                .setData(null)
                .setMessage(message)
                .setTimestamp(DateUtil.current());
    }
}
