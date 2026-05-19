package roomescape.member.domain.vo;

import roomescape.common.exception.DomainException;

import static roomescape.member.exception.MemberErrorCode.*;

public record Password(String password) {
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 20;

    public Password {
        validate(password);
    }

    private void validate(String password) {
        if (password == null || password.isBlank()) {
            throw new DomainException(EMPTY_PASSWORD);
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new DomainException(INVALID_PASSWORD_LENGTH);
        }

        if (password.chars().noneMatch(Character::isLetter)) {
            throw new DomainException(PASSWORD_MUST_CONTAIN_LETTER);
        }

        if (password.chars().noneMatch(Character::isDigit)) {
            throw new DomainException(PASSWORD_MUST_CONTAIN_DIGIT);
        }

        if (password.chars().anyMatch(Character::isWhitespace)) {
            throw new DomainException(PASSWORD_MUST_NOT_CONTAIN_WHITESPACE);
        }
    }
}
