package com.sun.guardian.example.common;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 通用返回结果封装
 * <p>
 * 模拟业务项目中的统一返回格式，用于演示 Guardian 与项目返回体系的集成。
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-13 11:26
 */
@Accessors(chain = true)
@Data
public class CommonResult<T> {

    /** 状态码：200 成功，500 失败 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /** 时间戳（毫秒） */
    private long timestamp;

    /**
     * 成功响应
     *
     * @param data 业务数据
     * @return 包含数据的成功结果
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
     *
     * @param message 错误提示信息
     * @return 不含数据的失败结果
     */
    public static <T> CommonResult<T> error(String message) {
        return new CommonResult<T>()
                .setCode(500)
                .setData(null)
                .setMessage(message)
                .setTimestamp(DateUtil.current());
    }
}
