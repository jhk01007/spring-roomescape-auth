package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.domain.LoginMemberInfo;
import roomescape.common.exception.DomainException;

import static roomescape.auth.domain.LoginMemberInfo.LOGIN_MEMBER_INFO;
import static roomescape.auth.exception.AuthErrorCode.AUTHENTICATION_ERROR;
import static roomescape.auth.exception.AuthErrorCode.AUTHORIZATION_ERROR;

@Component
public class AdminAuthorizeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LoginMemberInfo loginMemberInfo = (LoginMemberInfo) request.getAttribute(LOGIN_MEMBER_INFO);
        if (loginMemberInfo == null) {
            throw new DomainException(AUTHENTICATION_ERROR);
        }

        if (!loginMemberInfo.isAdmin()) {
            throw new DomainException(AUTHORIZATION_ERROR);
        }
        return true;
    }
}
