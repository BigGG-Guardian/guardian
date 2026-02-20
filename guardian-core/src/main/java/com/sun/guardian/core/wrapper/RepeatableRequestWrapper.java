package com.sun.guardian.core.wrapper;

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
 * @version java version 1.8
 * @since 2026-02-09
 */
@Getter
public class RepeatableRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    /** 从请求中读取并缓存请求体 */
    public RepeatableRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = IoUtil.readBytes(request.getInputStream());
    }

    /** 使用指定的 body 字节数组构造包装器 */
    protected RepeatableRequestWrapper(HttpServletRequest request, byte[] body) throws IOException {
        super(request);
        this.cachedBody = body;
    }

    /** 返回可重复读取的输入流 */
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

    /** 返回可重复读取的 BufferedReader */
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

}
