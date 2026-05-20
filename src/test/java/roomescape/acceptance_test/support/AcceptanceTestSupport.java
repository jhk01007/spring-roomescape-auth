package roomescape.acceptance_test.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.acceptance_test.support.auth.AuthStrategy;

public abstract class AcceptanceTestSupport {

    @LocalServerPort
    protected int port;

    @Autowired
    protected AuthStrategy authStrategy;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUpAcceptanceTest() throws JsonProcessingException {
        RestAssured.port = port;
        beforeAuthenticate();
        authStrategy.authenticate("test123", "password1", "test");
    }

    protected void beforeAuthenticate() {
    }

    @AfterEach
    void tearDownAcceptanceTest() {
        RestAssured.reset();
    }
}
