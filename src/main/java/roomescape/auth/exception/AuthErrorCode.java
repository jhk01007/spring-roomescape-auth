package roomescape.auth.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorPolicy;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public enum AuthErrorCode implements ErrorPolicy {
    INTERNAL_SERVER_ERROR("서버 내부에서 문제가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    AUTHENTICATION_ERROR("인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    AUTHORIZATION_ERROR("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    SESSION_NOT_FOUND("세션 정보가 존재하지 않습니다.", UNAUTHORIZED),
    TOKEN_NOT_FOUND("토큰 정보가 존재하지 않습니다.", UNAUTHORIZED),
    EXPIRED_TOKEN("만료된 토큰입니다.", UNAUTHORIZED),
    INVALID_TOKEN("유효하지 않는 토큰입니다.", UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;

    AuthErrorCode(String message, HttpStatus status) {
        this.code = name();
        this.message = message;
        this.status = status;
    }
    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
