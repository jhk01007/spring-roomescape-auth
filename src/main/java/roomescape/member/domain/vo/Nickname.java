package roomescape.member.domain.vo;

import roomescape.common.exception.DomainException;

import java.util.regex.Pattern;

import static roomescape.member.exception.MemberErrorCode.*;

public record Nickname(String nickname) {
    public static final int MIN_LENGTH = 2;
    public static final int MAX_LENGTH = 10;
    public static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9_]+$");

    public Nickname {
        validate(nickname);
    }

    private void validate(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new DomainException(EMPTY_NICKNAME);
        }

        if (nickname.length() < MIN_LENGTH || nickname.length() > MAX_LENGTH) {
            throw new DomainException(INVALID_NICKNAME_LENGTH);
        }

        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new DomainException(INVALID_NICKNAME_FORMAT);
        }
    }
}
