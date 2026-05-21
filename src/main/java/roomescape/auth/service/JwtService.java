package roomescape.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.infra.JwtProvider;
import roomescape.auth.domain.RefreshTokenRepository;
import roomescape.auth.service.dto.TokenIssueResult;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Role;

import static roomescape.auth.exception.AuthErrorCode.INVALID_TOKEN;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenIssueResult issueToken(Member member) {
        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getRole()); // 모바일이니까 쿠키가 아니라 응답 바디로 넘겨준다.

        refreshTokenRepository.save(
                member.getId(),
                refreshToken,
                jwtProvider.getExpiration(refreshToken));
        return new TokenIssueResult(accessToken, refreshToken);
    }

    public TokenIssueResult refresh(String reqRefreshToken) {
        Long memberId = jwtProvider.getMemberId(reqRefreshToken);
        Role role = jwtProvider.getRole(reqRefreshToken);
        validateIsRefreshToken(reqRefreshToken);
        validateRefreshToken(reqRefreshToken, getRefreshToken(memberId));

        String newAccessToken = jwtProvider.createAccessToken(memberId, role);
        String newRefreshToken = rotateRefreshToken(memberId, role);

        return new TokenIssueResult(newAccessToken, newRefreshToken);
    }

    private String getRefreshToken(Long memberId) {
        return refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DomainException(INVALID_TOKEN));
    }

    private String rotateRefreshToken(Long memberId, Role role) {
        refreshTokenRepository.deleteByMemberId(memberId);
        String newRefreshToken = jwtProvider.createRefreshToken(memberId, role);
        refreshTokenRepository.save(
                memberId,
                newRefreshToken,
                jwtProvider.getExpiration(newRefreshToken));
        return newRefreshToken;
    }

    public void logout(String refreshToken) {
        Long memberId = jwtProvider.getMemberId(refreshToken);
        validateIsRefreshToken(refreshToken);
        validateRefreshToken(refreshToken, getRefreshToken(memberId));
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    private void validateIsRefreshToken(String token) {
        if (!jwtProvider.isRefreshToken(token)) {
            throw new DomainException(INVALID_TOKEN);
        }
    }

    private static void validateRefreshToken(String refreshToken, String savedRefreshToken) {
        if(!refreshToken.equals(savedRefreshToken)) {
            throw new DomainException(INVALID_TOKEN);
        }
    }
}
