package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.infra.JwtProvider;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.GlobalErrorCode;


@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    public static final String LOGIN_MEMBER_ID = "loginMemberId";
    public static final String AUTH_EXCEPTION = "authException";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = getToken(request);
        if (token == null) return true;

        Long memberId = null;
        try {
            memberId = jwtProvider.getMemberId(token);
        } catch (DomainException e) {
            request.setAttribute(AUTH_EXCEPTION, e);
            return true;
        }
        request.setAttribute(LOGIN_MEMBER_ID, memberId);
        return true;
    }

    private static String getToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(AUTHORIZATION_PREFIX)) {
            request.setAttribute(AUTH_EXCEPTION, new DomainException(GlobalErrorCode.TOKEN_NOT_FOUND));
            return null;
        }

        String token = header.substring(AUTHORIZATION_PREFIX.length()).trim();
        return token;
    }

}
