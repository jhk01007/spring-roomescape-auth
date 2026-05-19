package roomescape.member.domain;

public class Member {

    private Long id;
    private final LoginId loginId;
    private final Password password;
    private final Nickname nickname;
    private final Role role;

    public Member(Long id, String nickname, String loginId, String password, Role role) {
        this.id = id;
        this.nickname = new Nickname(nickname);
        this.loginId = new LoginId(loginId);
        this.password = new Password(password);
        this.role = role;
    }

}
