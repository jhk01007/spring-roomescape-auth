package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.method.HandlerMethod;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.GlobalErrorCode;

@Component
public class RequiredAuthInterceptor implements HandlerInterceptor {
    public static final String LOGIN_MEMBER_ID = "loginMemberId";
    public static final String AUTH_EXCEPTION = "authException";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        if (request.getAttribute(LOGIN_MEMBER_ID) != null) {
            return true;
        }

        DomainException authException = (DomainException) request.getAttribute(AUTH_EXCEPTION);
        if (authException != null) {
            throw authException;
        }

        throw new DomainException(GlobalErrorCode.AUTHORIZATION_ERROR);
    }
}
