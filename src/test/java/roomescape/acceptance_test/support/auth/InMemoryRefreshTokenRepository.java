package roomescape.acceptance_test.support.auth;

import roomescape.auth.domain.RefreshTokenRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRefreshTokenRepository implements RefreshTokenRepository {

    private static final Map<Long, String> refreshTokenMap = new HashMap<>();

    @Override
    public void save(Long memberId, String refreshToken, Instant expiresAt) {
        refreshTokenMap.put(memberId, refreshToken);
    }

    @Override
    public Optional<String> findByMemberId(Long memberId) {
        return Optional.ofNullable(refreshTokenMap.get(memberId));
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        refreshTokenMap.remove(memberId);
    }
}
