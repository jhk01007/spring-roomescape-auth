package roomescape.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.member.exception.MemberErrorCode.*;

class NicknameTest {

    @Test
    @DisplayName("닉네임이 null이면 도메인 예외가 발생한다.")
    void create_fail1() {
        assertDomainException(
                () -> new Nickname(null),
                EMPTY_NICKNAME
        );
    }

    @Test
    @DisplayName("닉네임의 길이가 유효하지 않으면 도메인 예외가 발생한다.")
    void create_fail2() {
        assertDomainException(
                () -> new Nickname("김"),
                INVALID_NICKNAME_LENGTH
        );
    }

    @Test
    @DisplayName("닉네임의 형식이 유효하지 않으면 도메인 예외가 발생한다.")
    void create_fail3() {
        assertDomainException(
                () -> new Nickname("닉네임!"),
                INVALID_NICKNAME_FORMAT
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
