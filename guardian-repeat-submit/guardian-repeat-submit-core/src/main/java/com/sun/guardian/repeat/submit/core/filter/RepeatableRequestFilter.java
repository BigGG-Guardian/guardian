package com.sun.guardian.repeat.submit.core.filter;

import com.sun.guardian.core.utils.RepeatableRequestWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求体缓存过滤器，仅对 JSON 请求进行包装以支持重复读取 body
 *
 * @author scj
 * @since 2026-02-09
 */
public class RepeatableRequestFilter extends OncePerRequestFilter {

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
