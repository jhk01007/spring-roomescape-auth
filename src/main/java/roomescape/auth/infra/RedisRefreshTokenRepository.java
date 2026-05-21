package roomescape.auth.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import roomescape.auth.domain.RefreshTokenRepository;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;

    public void save(Long memberId, String refreshToken, Instant expiresAt) {
        String key = key(memberId);
        redisTemplate.opsForValue().set(key, refreshToken);
        redisTemplate.expireAt(key, Date.from(expiresAt));
    }

    public Optional<String> findByMemberId(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(memberId)));
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        redisTemplate.delete(key(memberId));
    }

    private static String key(Long memberId) {
        return "refresh-token:" + memberId;
    }
}
