package roomescape.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.infra.JwtProvider;
import roomescape.auth.infra.RefreshTokenRepository;
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

    public String refresh(String refreshToken) {
        Long memberId = jwtProvider.getMemberId(refreshToken);
        String savedRefreshToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DomainException(GlobalErrorCode.INVALID_TOKEN));
        if(!refreshToken.equals(savedRefreshToken)) {
            throw new DomainException(GlobalErrorCode.INVALID_TOKEN);
        }
        return jwtProvider.createAccessToken(memberId);
    }
}
