package roomescape.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberErrorCode;
import roomescape.member.repository.JdbcMemberRepository;
import roomescape.member.repository.MemberRepository;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@Import({
        AuthService.class,
        JdbcMemberRepository.class
})
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("로그인에 성공하면 성공한 Member 객체를 반환한다.")
    public void login_success() {
        // given
        String loginId = "login1";
        String password = "password1";
        String nickname = "닉네임";
        memberRepository.save(Member.user(loginId, password, nickname));

        // when
        Member loginMember = authService.login(loginId, password);

        // then
        assertThat(loginMember)
                .extracting(
                        Member::getLoginId,
                        Member::getPassword,
                        Member::getNickname
                ).containsExactly(loginId, password, nickname);
    }

    @Test
    @DisplayName("로그인할 때 아이디가 존재하지 않으면 예외가 발생한다.")
    public void login_fail1() {
        // given
        String loginId = "login1";
        String password = "password1";

        // when then
        assertThatThrownBy(() -> authService.login(loginId, password))
                .isInstanceOf(DomainException.class)
                .hasMessage(MemberErrorCode.INVALID_LOGIN_CREDENTIALS.message());
    }

    @Test
    @DisplayName("로그인할 때 비밀번호가 일치하지 않으면 예외가 발생한다.")
    public void login_fail2() {
        // given
        String loginId = "login1";
        String password = "password1";
        String nickname = "닉네임";
        memberRepository.save(Member.user(loginId, password, nickname));

        // when then
        assertThatThrownBy(() -> authService.login(loginId, "password2"))
                .isInstanceOf(DomainException.class)
                .hasMessage(MemberErrorCode.INVALID_LOGIN_CREDENTIALS.message());
    }
}
