package roomescape.auth.controller.dto;

public record TokenRefreshRequest(
        String refreshToken
) {
}
