package roomescape.auth.controller.dto;

import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Role;

public record LoginMemberInfo(
        Long id,
        Role role
) {
    public static LoginMemberInfo from(Member member) {
        return new LoginMemberInfo(member.getId(), member.getRole());
    }
}
