package roomescape.member.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberCreateRequest(
        @NotBlank String loginId,
        @NotBlank String password,
        @NotBlank String nickname
) {
}
