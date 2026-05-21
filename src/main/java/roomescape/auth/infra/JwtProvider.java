package roomescape.auth.infra;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.auth.exception.AuthErrorCode;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.GlobalErrorCode;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

import static roomescape.auth.exception.AuthErrorCode.*;
import static roomescape.auth.exception.AuthErrorCode.INVALID_TOKEN;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    private static final String TOKEN_TYPE = "type";
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

    public String createAccessToken(Long memberId) {
        return createToken(accessTokenExpirationMs, memberId, ACCESS_TOKEN);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(refreshTokenExpirationMs, memberId, REFRESH_TOKEN);
    }

    private String createToken(long refreshTokenExpirationMs, Long memberId, String type) {

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenExpirationMs);
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(TOKEN_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public Long getMemberId(String token) throws DomainException {
        try {
            return Long.valueOf(Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject());
        } catch (ExpiredJwtException e) {
            throw new DomainException(EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new DomainException(INVALID_TOKEN);
        }
    }

    public Instant getExpiration(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .toInstant();
        } catch (ExpiredJwtException e) {
            throw new DomainException(EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new DomainException(INVALID_TOKEN);
        }
    }

    public boolean isAccessToken(String token) {
        try {
            return ACCESS_TOKEN.equals(Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get(TOKEN_TYPE));
        } catch (ExpiredJwtException e) {
            throw new DomainException(EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new DomainException(INVALID_TOKEN);
        }
    }


}
