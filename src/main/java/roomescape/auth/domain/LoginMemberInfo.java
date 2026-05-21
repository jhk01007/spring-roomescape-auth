package roomescape.auth.domain;

import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Role;

public record LoginMemberInfo(
        Long id,
        Role role
) {

    public static final String LOGIN_MEMBER_INFO = "loginMemberInfo";

    public static LoginMemberInfo from(Member member) {
        return new LoginMemberInfo(member.getId(), member.getRole());
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
