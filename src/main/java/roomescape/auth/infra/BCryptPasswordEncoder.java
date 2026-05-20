package roomescape.auth.infra;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;
import roomescape.auth.domain.PasswordEncoder;

@Component
public class BCryptPasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
