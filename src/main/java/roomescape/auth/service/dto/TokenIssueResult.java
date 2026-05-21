package roomescape.auth.service.dto;

public record TokenIssueResult(
        String accessToken,
        String refreshToken
) {
}
