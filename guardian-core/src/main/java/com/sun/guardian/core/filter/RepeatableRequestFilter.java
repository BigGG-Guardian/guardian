package com.sun.guardian.core.filter;

import com.sun.guardian.core.wrapper.RepeatableRequestWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求体缓存过滤器，对 JSON 请求包装以支持重复读取 body
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-09
 */
public class RepeatableRequestFilter extends OncePerRequestFilter {

    /** 对 JSON 请求包装为可重复读取的请求 */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            filterChain.doFilter(new RepeatableRequestWrapper(request), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
