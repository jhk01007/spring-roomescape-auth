package roomescape.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberLoginRequest(
        @NotBlank String loginId,
        @NotBlank String password
) {
}
