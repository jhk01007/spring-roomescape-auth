package roomescape.acceptance_test.support.auth;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.auth.infra.RefreshTokenRepository;

@TestConfiguration
public class TestAuthConfig {
    @Bean
    public AuthStrategy authStrategy() {
        return new MobileAuthStrategy();
    }

    @Bean
    public RefreshTokenRepository refreshTokenRepository() {
        return new InMemoryRefreshTokenRepository();
    }
}
