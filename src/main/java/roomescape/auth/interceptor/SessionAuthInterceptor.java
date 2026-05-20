package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.GlobalErrorCode;

@Component
public class SessionAuthInterceptor implements HandlerInterceptor {
    public static final String LOGIN_MEMBER_ID = "loginMemberId";
    public static final String AUTH_EXCEPTION = "authException";
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(LOGIN_MEMBER_ID) == null) {
            request.setAttribute(AUTH_EXCEPTION, new DomainException(GlobalErrorCode.SESSION_NOT_FOUND));
            return true;
        }
        request.setAttribute(LOGIN_MEMBER_ID, session.getAttribute(LOGIN_MEMBER_ID));
        return true;
    }
}
