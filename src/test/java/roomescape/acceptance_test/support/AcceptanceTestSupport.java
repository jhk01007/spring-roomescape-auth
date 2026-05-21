package roomescape.acceptance_test.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.acceptance_test.support.auth.AuthStrategy;

public abstract class AcceptanceTestSupport {

    @LocalServerPort
    protected int port;

    @Autowired
    protected AuthStrategy authStrategy;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpAcceptanceTest() throws JsonProcessingException {
        RestAssured.port = port;
        setUpDefaultStore();
        beforeAuthenticate();
        authStrategy.authenticate("test123", "password1", "test");
    }

    protected void beforeAuthenticate() {
    }

    private void setUpDefaultStore() {
        jdbcTemplate.update("""
                MERGE INTO store (id, name) KEY(id)
                VALUES (1, '잠실점')
                """);
    }

    @AfterEach
    void tearDownAcceptanceTest() {
        RestAssured.reset();
    }
}
