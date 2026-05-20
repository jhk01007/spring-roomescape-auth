package roomescape.common.exception;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public enum GlobalErrorCode implements ErrorPolicy {

    VALIDATION_ERROR("Validation 오류입니다.", BAD_REQUEST),
    INVALID_AUTHENTICATION_HEADER("잘못된 형식의 인증 헤더 입니다.", BAD_REQUEST),
    INVALID_GUEST_NAME_HEADER("잘못된 형식의 예약자 이름 헤더 입니다.", BAD_REQUEST),
    INTERNAL_SERVER_ERROR("서버 내부에서 문제가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    AUTHORIZATION_ERROR("인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    SESSION_NOT_FOUND("세션 정보가 존재하지 않습니다.", BAD_REQUEST),
    TOKEN_NOT_FOUND("토큰이 존재하지 않습니다.", BAD_REQUEST),
    EXPIRED_TOKEN("만료된 토큰입니다.", BAD_REQUEST),
    INVALID_TOKEN("유효하지 않는 토큰입니다.", BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    GlobalErrorCode(String message, HttpStatus status) {
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
