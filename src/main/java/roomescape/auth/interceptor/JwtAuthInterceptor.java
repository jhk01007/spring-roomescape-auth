package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.infra.JwtProvider;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.GlobalErrorCode;

import static roomescape.auth.exception.AuthErrorCode.INVALID_TOKEN;
import static roomescape.auth.exception.AuthErrorCode.TOKEN_NOT_FOUND;


@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    public static final String LOGIN_MEMBER_ID = "loginMemberId";
    public static final String AUTH_EXCEPTION = "authException";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = getToken(request);
        if (token == null) return true;

        Long memberId = getMemberId(request, token);
        if (memberId == null) return true;

        request.setAttribute(LOGIN_MEMBER_ID, memberId);
        return true;
    }

    private Long getMemberId(HttpServletRequest request, String token) {
        Long memberId;
        try {
            validateIsAccessToken(token);
            memberId = jwtProvider.getMemberId(token);
        } catch (DomainException e) {
            request.setAttribute(AUTH_EXCEPTION, e);
            return null;
        }
        return memberId;
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
