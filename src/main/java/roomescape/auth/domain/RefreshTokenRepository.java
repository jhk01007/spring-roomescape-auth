package roomescape.auth.domain;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {
    void save(Long memberId, String refreshToken, Instant expiresAt);
    Optional<String> findByMemberId(Long memberId);
    void deleteByMemberId(Long memberId);
}
