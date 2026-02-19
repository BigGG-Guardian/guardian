package com.sun.guardian.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 通用返回
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-19 15:42
 */
@Accessors(chain = true)
@Data
public class BaseResult {
    private int code;

    private Object data;

    private String msg;

    private long timestamp;

    public static BaseResult success(Object data) {
        return new BaseResult()
                .setCode(200)
                .setData(data)
                .setMsg("success")
                .setTimestamp(System.currentTimeMillis());
    }

    public static BaseResult error(String msg) {
        return new BaseResult()
                .setCode(500)
                .setData(null)
                .setMsg(msg)
                .setTimestamp(System.currentTimeMillis());
    }

    public static BaseResult result(int code, Object data, String msg) {
        return new BaseResult()
                .setCode(code)
                .setData(data)
                .setMsg(msg)
                .setTimestamp(System.currentTimeMillis());
    }
}
