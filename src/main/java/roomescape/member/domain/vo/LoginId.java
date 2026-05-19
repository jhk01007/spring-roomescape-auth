package roomescape.member.domain.vo;

import roomescape.common.exception.DomainException;

import java.util.regex.Pattern;

import static roomescape.member.exception.MemberErrorCode.*;

public record LoginId(String loginId) {
    public static final int MIN_LENGTH = 4;
    public static final int MAX_LENGTH = 20;
    public static final Pattern LOGIN_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    public LoginId {
        validate(loginId);
    }

    private void validate(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw new DomainException(EMPTY_LOGIN_ID);
        }

        if (loginId.length() < MIN_LENGTH || loginId.length() > MAX_LENGTH) {
            throw new DomainException(INVALID_LOGIN_ID_LENGTH);
        }

        if (!LOGIN_ID_PATTERN.matcher(loginId).matches()) {
            throw new DomainException(INVALID_LOGIN_ID_FORMAT);
        }
    }
}
