package roomescape.member.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberCreateDto(
        @NotBlank String loginId,
        @NotBlank String password,
        @NotBlank String nickname
) {
}
