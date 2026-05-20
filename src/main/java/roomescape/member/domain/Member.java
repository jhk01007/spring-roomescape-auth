package roomescape.member.domain;

import lombok.Getter;
import roomescape.member.domain.vo.LoginId;
import roomescape.member.domain.vo.Nickname;
import roomescape.member.domain.vo.Password;
import roomescape.member.domain.vo.Role;

import java.util.Objects;

@Getter
public class Member {

    private Long id;
    private final LoginId loginId;
    private final Password password;
    private final Nickname nickname;
    private final Role role;

    private Member(Long id, LoginId loginId, Password password, Nickname nickname, Role role) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    public static Member user(String loginId, Password password, String nickname) {
        return new Member(null, new LoginId(loginId), password, new Nickname(nickname), Role.USER);
    }

    public static Member of(Long id, String loginId, Password password, String nickname, Role role) {
        return new Member(id, new LoginId(loginId), password, new Nickname(nickname), role);
    }

    public Member withId(Long id) {
        return new Member(id, loginId, password, nickname, role);
    }

    public String getLoginId() {
        return loginId.loginId();
    }

    public String getPassword() {
        return password.password();
    }

    public String getNickname() {
        return nickname.nickname();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member member)) return false;
        return id != null && Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
