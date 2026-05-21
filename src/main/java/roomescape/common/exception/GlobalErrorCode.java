package roomescape.common.exception;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public enum GlobalErrorCode implements ErrorPolicy {

    VALIDATION_ERROR("Validation 오류입니다.", BAD_REQUEST),
    NOT_FOUND("요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

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
