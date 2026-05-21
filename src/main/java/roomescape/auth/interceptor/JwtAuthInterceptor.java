package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.domain.LoginMemberInfo;
import roomescape.auth.infra.JwtProvider;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.vo.Role;

import static roomescape.auth.domain.LoginMemberInfo.LOGIN_MEMBER_INFO;
import static roomescape.auth.exception.AuthErrorCode.INVALID_TOKEN;
import static roomescape.auth.exception.AuthErrorCode.TOKEN_NOT_FOUND;


@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    public static final String AUTH_EXCEPTION = "authException";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = getToken(request);
        if (token == null) return true;

        LoginMemberInfo loginMemberInfo = getLoginMemberInfo(request, token);
        if (loginMemberInfo == null) return true;

        request.setAttribute(LOGIN_MEMBER_INFO, loginMemberInfo);
        return true;
    }

    private LoginMemberInfo getLoginMemberInfo(HttpServletRequest request, String token) {
        try {
            validateIsAccessToken(token);
            Long memberId = jwtProvider.getMemberId(token);
            Role role = jwtProvider.getRole(token);
            return new LoginMemberInfo(memberId, role);
        } catch (DomainException e) {
            request.setAttribute(AUTH_EXCEPTION, e);
            return null;
        }
    }

    private void validateIsAccessToken(String token) {
        if(!jwtProvider.isAccessToken(token)) {
            throw new DomainException(INVALID_TOKEN);
        }
    }

    private static String getToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(AUTHORIZATION_PREFIX)) {
            request.setAttribute(AUTH_EXCEPTION, new DomainException(TOKEN_NOT_FOUND));
            return null;
        }

        return header.substring(AUTHORIZATION_PREFIX.length()).trim();
    }

}
