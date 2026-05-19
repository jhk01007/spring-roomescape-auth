package roomescape.member.controller.dto;

import roomescape.member.domain.Member;

public record MemberCreateResponse(
        Long id,
        String loginId,
        String nickname,
        String role
) {

    public static MemberCreateResponse from(Member member) {
        return new MemberCreateResponse(
                member.getId(), member.getLoginId(), member.getNickname(), member.getRole().name());
    }
}
