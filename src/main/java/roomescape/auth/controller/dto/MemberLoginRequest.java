package roomescape.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberLoginRequest(
        @NotBlank(message = "ID는 필수입니다.") String loginId,
        @NotBlank(message = "비밀번호는 필수입니다.") String password
) {
}
