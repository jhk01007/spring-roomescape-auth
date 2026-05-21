package roomescape.auth.infra;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.vo.Role;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

import static roomescape.auth.exception.AuthErrorCode.EXPIRED_TOKEN;
import static roomescape.auth.exception.AuthErrorCode.INVALID_TOKEN;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    private static final String TOKEN_TYPE = "type";
    private static final String ROLE = "role";
    private static final String ACCESS_TOKEN = "ACCESS";
    private static final String REFRESH_TOKEN = "REFRESH";

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String createAccessToken(Long memberId, Role role) {
        return createToken(accessTokenExpirationMs, memberId, role, ACCESS_TOKEN);
    }

    public String createRefreshToken(Long memberId, Role role) {
        return createToken(refreshTokenExpirationMs, memberId, role, REFRESH_TOKEN);
    }

    private String createToken(long refreshTokenExpirationMs, Long memberId, Role role, String type) {

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenExpirationMs);
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(TOKEN_TYPE, type)
                .claim(ROLE, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public Long getMemberId(String token) throws DomainException {
        try {
            return Long.valueOf(getClaims(token).getSubject());
        } catch (ExpiredJwtException e) {
            throw new DomainException(EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new DomainException(INVALID_TOKEN);
        }
    }

    public Role getRole(String token) {
        try {
            return Role.valueOf(getClaims(token).get(ROLE, String.class));
        } catch (ExpiredJwtException e) {
            throw new DomainException(EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            throw new DomainException(INVALID_TOKEN);
        }
    }

    public Instant getExpiration(String token) {
        try {
            return getClaims(token)
                    .getExpiration()
                    .toInstant();
        } catch (ExpiredJwtException e) {
            throw new DomainException(EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new DomainException(INVALID_TOKEN);
        }
    }

    public boolean isAccessToken(String token) {
        return isTokenType(token, ACCESS_TOKEN);
    }

    public boolean isRefreshToken(String token) {
        return isTokenType(token, REFRESH_TOKEN);
    }

    private boolean isTokenType(String token, String tokenType) {
        try {
            return tokenType.equals(getClaims(token).get(TOKEN_TYPE));
        } catch (ExpiredJwtException e) {
            throw new DomainException(EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new DomainException(INVALID_TOKEN);
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
