package roomescape.acceptance_test.support.auth;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface AuthStrategy {
    void authenticate(String loginId, String password, String nickname) throws JsonProcessingException;
}
