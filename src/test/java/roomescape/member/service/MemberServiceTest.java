package roomescape.member.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Role;
import roomescape.member.exception.MemberErrorCode;
import roomescape.member.repository.JdbcMemberRepository;
import roomescape.member.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({
        MemberService.class,
        JdbcMemberRepository.class
}
)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;


    @Test
    @DisplayName("회원가입을 하면 Role이 USER인 사용자가 생성된다.")
    public void signup_success() {
        // given
        String loginId = "login1";
        String password = "password1";
        String nickname = "닉네임";

        // when
        Member member = memberService.signUp(loginId, password, nickname);

        // then
        assertThat(member.getId()).isNotNull();
        assertThat(member).extracting(
                Member::getLoginId,
                Member::getPassword,
                Member::getNickname,
                Member::getRole
        ).containsExactly(loginId, password, nickname, Role.USER);
    }

    @Test
    @DisplayName("회원가입을 할 때 아이디가 중복되면 예외가 발생한다.")
    public void signup_fail1() {
        // given
        String loginId = "login1";
        String password = "password1";
        String nickname = "닉네임";
        memberRepository.save(Member.user(loginId, password, nickname));

        // when then
        assertThatThrownBy(() -> memberService.signUp(loginId, password + "_", nickname + "_"))
                .isInstanceOf(DomainException.class)
                .hasMessage(MemberErrorCode.LOGIN_ID_ALREADY_EXISTS.message());
    }

    @Test
    @DisplayName("회원가입을 할 때 닉네임이 중복되면 예외가 발생한다.")
    public void signup_fail2() {
        // given
        String loginId = "login1";
        String password = "password1";
        String nickname = "닉네임";
        memberRepository.save(Member.user(loginId, password, nickname));

        // when then
        assertThatThrownBy(() -> memberService.signUp(loginId + "_", password, nickname))
                .isInstanceOf(DomainException.class)
                .hasMessage(MemberErrorCode.NICKNAME_ALREADY_EXISTS.message());
    }


}
