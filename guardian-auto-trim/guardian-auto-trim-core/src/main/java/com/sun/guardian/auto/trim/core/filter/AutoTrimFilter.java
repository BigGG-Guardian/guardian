package com.sun.guardian.auto.trim.core.filter;

import com.sun.guardian.auto.trim.core.config.AutoTrimConfig;
import com.sun.guardian.auto.trim.core.wrapper.AutoTrimRequestWrapper;
import com.sun.guardian.core.utils.CharacterSanitizer;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求参数自动 trim 过滤器
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 15:22
 */
public class AutoTrimFilter extends OncePerRequestFilter {

    private final AutoTrimConfig autoTrimConfig;
    private final CharacterSanitizer sanitizer;

    /**
     * 构造过滤器
     */
    public AutoTrimFilter(AutoTrimConfig autoTrimConfig, CharacterSanitizer sanitizer) {
        this.autoTrimConfig = autoTrimConfig;
        this.sanitizer = sanitizer;
    }

    /**
     * 将请求包装为 AutoTrimRequestWrapper 执行参数 trim
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        AutoTrimRequestWrapper wrapper = new AutoTrimRequestWrapper(request, autoTrimConfig, sanitizer);
        filterChain.doFilter(wrapper, response);
    }
}
