package roomescape.member.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorPolicy;

import static org.springframework.http.HttpStatus.*;

public enum MemberErrorCode implements ErrorPolicy {
    EMPTY_LOGIN_ID("로그인 아이디는 비어 있을 수 없습니다.", BAD_REQUEST),
    INVALID_LOGIN_ID_LENGTH("로그인 아이디는 4자 이상 20자 이하여야 합니다.", BAD_REQUEST),
    INVALID_LOGIN_ID_FORMAT("로그인 아이디는 영문, 숫자, 언더스코어만 사용할 수 있습니다.", BAD_REQUEST),

    EMPTY_PASSWORD("비밀번호는 비어 있을 수 없습니다.", BAD_REQUEST),
    INVALID_PASSWORD_LENGTH("비밀번호는 8자 이상 20자 이하여야 합니다.", BAD_REQUEST),
    PASSWORD_MUST_CONTAIN_LETTER("비밀번호는 영문을 포함해야 합니다.", BAD_REQUEST),
    PASSWORD_MUST_CONTAIN_DIGIT("비밀번호는 숫자를 포함해야 합니다.", BAD_REQUEST),
    PASSWORD_MUST_NOT_CONTAIN_WHITESPACE("비밀번호는 공백을 포함할 수 없습니다.", BAD_REQUEST),

    EMPTY_NICKNAME("닉네임은 비어 있을 수 없습니다.", BAD_REQUEST),
    INVALID_NICKNAME_LENGTH("닉네임은 2자 이상 10자 이하여야 합니다.", BAD_REQUEST),
    INVALID_NICKNAME_FORMAT("닉네임은 한글, 영문, 숫자, 언더스코어만 사용할 수 있습니다.", BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    MemberErrorCode(String message, HttpStatus status) {
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
