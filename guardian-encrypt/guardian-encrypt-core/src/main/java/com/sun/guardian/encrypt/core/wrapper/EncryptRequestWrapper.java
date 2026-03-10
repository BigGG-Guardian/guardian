package com.sun.guardian.encrypt.core.wrapper;

import com.sun.guardian.core.wrapper.RepeatableRequestWrapper;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 加解密请求包装器
 * @author scj
 * @version java version 1.8
 * @since 2026-03-06 16:45
 */
@Setter
public class EncryptRequestWrapper extends HttpServletRequestWrapper {

    @Getter
    private byte[] cachedBody;

    private Map<String, String[]> parameters;

    public EncryptRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        if (request instanceof RepeatableRequestWrapper) {
            this.cachedBody = ((RepeatableRequestWrapper) request).getCachedBody();
        } else {
            this.cachedBody = new byte[0];
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody != null ? cachedBody : new byte[0]);
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

    @Override
    public Map<String, String[]> getParameterMap() {
        if (parameters != null && !parameters.isEmpty()) {
            return parameters;
        }
        return super.getParameterMap();
    }

    @Override
    public String getParameter(String name) {
        if (parameters != null && parameters.containsKey(name)) {
            String[] values = parameters.get(name);
            return values != null && values.length > 0 ? values[0] : null;
        }
        return super.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        if (parameters != null && parameters.containsKey(name)) {
            return parameters.get(name);
        }
        return super.getParameterValues(name);
    }

}
