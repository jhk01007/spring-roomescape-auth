package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.GlobalErrorCode;

import static roomescape.auth.interceptor.AuthConst.LOGIN_MEMBER_ID;

public class UserAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(LOGIN_MEMBER_ID) == null) {
            throw new DomainException(GlobalErrorCode.AUTHORIZATION_ERROR);
        }
        request.setAttribute(LOGIN_MEMBER_ID, session.getAttribute(LOGIN_MEMBER_ID));
        return true;
    }
}
