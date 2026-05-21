package roomescape.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.infra.JwtProvider;
import roomescape.auth.domain.RefreshTokenRepository;
import roomescape.auth.service.dto.TokenIssueResult;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.GlobalErrorCode;
import roomescape.member.domain.Member;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenIssueResult issueToken(Member member) {
        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId()); // 모바일이니까 쿠키가 아니라 응답 바디로 넘겨준다.

        refreshTokenRepository.save(
                member.getId(),
                refreshToken,
                jwtProvider.getExpiration(refreshToken));
        return new TokenIssueResult(accessToken, refreshToken);
    }

    public TokenIssueResult refresh(String reqRefreshToken) {
        Long memberId = jwtProvider.getMemberId(reqRefreshToken);
        validateRefreshToken(reqRefreshToken, getRefreshToken(memberId));

        String newAccessToken = jwtProvider.createAccessToken(memberId);
        String newRefreshToken = rotateRefreshToken(memberId);

        return new TokenIssueResult(newAccessToken, newRefreshToken);
    }

    private String getRefreshToken(Long memberId) {
        return refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DomainException(GlobalErrorCode.INVALID_TOKEN));
    }

    private String rotateRefreshToken(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
        String newRefreshToken = jwtProvider.createRefreshToken(memberId);
        refreshTokenRepository.save(
                memberId,
                newRefreshToken,
                jwtProvider.getExpiration(newRefreshToken));
        return newRefreshToken;
    }

    public void logout(String refreshToken) {
        Long memberId = jwtProvider.getMemberId(refreshToken);
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    private static void validateRefreshToken(String refreshToken, String savedRefreshToken) {
        if(!refreshToken.equals(savedRefreshToken)) {
            throw new DomainException(GlobalErrorCode.INVALID_TOKEN);
        }
    }
}
