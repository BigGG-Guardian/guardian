package com.sun.guardian.trace.core.filter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * 请求链路 TraceId 过滤器，自动生成或透传 TraceId 并写入 MDC
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 19:37
 */
public class TraceIdFilter extends OncePerRequestFilter {

    private final String headerName;
    private static final String MDC_KEY = "traceId";

    /**
     * 构造 TraceId 过滤器
     */
    public TraceIdFilter(String headerName) {
        this.headerName = headerName;
    }

    /**
     * 从请求头获取或自动生成 TraceId，放入 MDC 并写入响应头
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = request.getHeader(headerName);
            if (StrUtil.isBlank(traceId)) {
                traceId = generateTraceId();
            }
            MDC.put(MDC_KEY, traceId);
            response.setHeader(headerName, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    /**
     * 生成 TraceId（时分秒 + 10位随机字符串）
     */
    private String generateTraceId() {
        return DateUtil.format(new Date(), "HHmmss") + RandomUtil.randomString(10);
    }
}
