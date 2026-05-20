package roomescape.acceptance_test.support.auth;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AuthStrategyConfig {
    @Bean
    public AuthStrategy authStrategy() {
        return new TokenAuthStrategy();
    }
}
