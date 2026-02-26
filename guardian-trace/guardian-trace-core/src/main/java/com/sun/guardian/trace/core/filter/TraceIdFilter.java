package com.sun.guardian.trace.core.filter;

import com.sun.guardian.trace.core.config.TraceConfig;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 请求链路 TraceId 过滤器，自动生成或透传 TraceId 并写入 MDC
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 19:37
 */
public class TraceIdFilter extends OncePerRequestFilter {

    private final TraceConfig traceConfig;
    public static final String MDC_KEY = "traceId";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 构造 TraceId 过滤器
     */
    public TraceIdFilter(TraceConfig traceConfig) {
        this.traceConfig = traceConfig;
    }

    /**
     * 从请求头获取或自动生成 TraceId，放入 MDC 并写入响应头
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = request.getHeader(traceConfig.getHeaderName());
            if (traceId == null || traceId.trim().isEmpty()) {
                traceId = generateTraceId();
            }
            MDC.put(MDC_KEY, traceId);
            response.setHeader(traceConfig.getHeaderName(), traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    /**
     * 生成 TraceId（时分秒 + 10位随机字符串）
     */
    private String generateTraceId() {
        StringBuilder sb = new StringBuilder(16);
        sb.append(LocalTime.now().format(TIME_FMT));
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 10; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
