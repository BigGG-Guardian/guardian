package com.sun.guardian.core.utils;

import cn.hutool.core.io.IoUtil;
import lombok.Getter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 可重复读取请求体的 HttpServletRequestWrapper
 *
 * @author scj
 * @since 2026-02-09
 */
@Getter
public class RepeatableRequestWrapper extends HttpServletRequestWrapper {

    /**
     * -- GETTER --
     * 获取缓存的请求体字节数组
     */
    private final byte[] cachedBody;

    public RepeatableRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = IoUtil.readBytes(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() {
                return bais.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

}
