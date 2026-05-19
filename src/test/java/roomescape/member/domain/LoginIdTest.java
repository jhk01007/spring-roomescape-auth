package roomescape.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorPolicy;
import roomescape.member.domain.vo.LoginId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.member.exception.MemberErrorCode.*;

class LoginIdTest {

    @Test
    @DisplayName("로그인 아이디가 null이면 도메인 예외가 발생한다.")
    void create_fail1() {
        assertDomainException(
                () -> new LoginId(null),
                EMPTY_LOGIN_ID
        );
    }

    @Test
    @DisplayName("로그인 아이디의 길이가 유효하지 않으면 도메인 예외가 발생한다.")
    void create_fail2() {
        assertDomainException(
                () -> new LoginId("abc"),
                INVALID_LOGIN_ID_LENGTH
        );
    }

    @Test
    @DisplayName("로그인 아이디의 형식이 유효하지 않으면 도메인 예외가 발생한다.")
    void create_fail3() {
        assertDomainException(
                () -> new LoginId("abcd!"),
                INVALID_LOGIN_ID_FORMAT
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
