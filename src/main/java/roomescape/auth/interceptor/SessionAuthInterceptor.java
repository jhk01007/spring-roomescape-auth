package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.domain.LoginMemberInfo;
import roomescape.common.exception.DomainException;

import static roomescape.auth.domain.LoginMemberInfo.LOGIN_MEMBER_INFO;
import static roomescape.auth.exception.AuthErrorCode.SESSION_NOT_FOUND;

@Component
public class SessionAuthInterceptor implements HandlerInterceptor {
    public static final String AUTH_EXCEPTION = "authException";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(LOGIN_MEMBER_INFO) instanceof LoginMemberInfo loginMemberInfo)) {
            request.setAttribute(AUTH_EXCEPTION, new DomainException(SESSION_NOT_FOUND));
            return true;
        }
        request.setAttribute(LOGIN_MEMBER_INFO, loginMemberInfo);
        return true;
    }
}
