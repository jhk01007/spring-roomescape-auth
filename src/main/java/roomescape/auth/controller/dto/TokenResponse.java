package roomescape.auth.controller.dto;

import roomescape.auth.service.dto.TokenIssueResult;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {

    public static TokenResponse from(TokenIssueResult tokenIssueResult) {
        return new TokenResponse(tokenIssueResult.accessToken(), tokenIssueResult.refreshToken());
    }
}
