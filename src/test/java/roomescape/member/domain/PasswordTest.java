package roomescape.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorPolicy;
import roomescape.member.domain.vo.Password;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.member.exception.MemberErrorCode.*;

class PasswordTest {

    @Test
    @DisplayName("비밀번호가 null이면 도메인 예외가 발생한다.")
    void create_fail1() {
        assertDomainException(
                () -> new Password(null),
                EMPTY_PASSWORD
        );
    }

    @Test
    @DisplayName("비밀번호의 길이가 유효하지 않으면 도메인 예외가 발생한다.")
    void create_fail2() {
        assertDomainException(
                () -> new Password("abc123"),
                INVALID_PASSWORD_LENGTH
        );
    }

    @Test
    @DisplayName("비밀번호에 영문이 없으면 도메인 예외가 발생한다.")
    void create_fail3() {
        assertDomainException(
                () -> new Password("12345678"),
                PASSWORD_MUST_CONTAIN_LETTER
        );
    }

    @Test
    @DisplayName("비밀번호에 숫자가 없으면 도메인 예외가 발생한다.")
    void create_fail4() {
        assertDomainException(
                () -> new Password("password"),
                PASSWORD_MUST_CONTAIN_DIGIT
        );
    }

    @Test
    @DisplayName("비밀번호에 공백이 있으면 도메인 예외가 발생한다.")
    void create_fail5() {
        assertDomainException(
                () -> new Password("password 1"),
                PASSWORD_MUST_NOT_CONTAIN_WHITESPACE
        );
    }

    private void assertDomainException(Runnable runnable, ErrorPolicy errorCode) {
        assertThatThrownBy(runnable::run)
                .isInstanceOfSatisfying(DomainException.class, exception ->
                        assertThat(exception.getErrorPolicy()).isEqualTo(errorCode)
                )
                .hasMessage(errorCode.message());
    }
}
