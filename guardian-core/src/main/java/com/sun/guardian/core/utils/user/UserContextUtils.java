package com.sun.guardian.core.utils.user;

import com.sun.guardian.core.context.UserContext;
import com.sun.guardian.core.utils.ip.IpUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 用户上下文工具类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-15 17:05
 */
public class UserContextUtils {

    private final UserContext userContext;

    /**
     * 构造用户上下文工具
     */
    public UserContextUtils(UserContext userContext) {
        this.userContext = userContext;
    }

    /**
     * 解析用户标识：userId → sessionId → IP
     */
    public String resolveUserId(HttpServletRequest request) {
        String userId = userContext.getUserId();
        if (StringUtils.hasText(userId)) {
            return userId;
        }
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : IpUtils.getClientIp(request);
    }
}
