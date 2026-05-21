package roomescape.auth.infra;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {
    void save(Long memberId, String refreshToken, Instant expiresAt);
    Optional<String> findByMemberId(Long memberId);
}
