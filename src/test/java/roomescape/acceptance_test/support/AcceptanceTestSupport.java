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
        setUpDefaultStoreManager("test123");
    }

    protected void beforeAuthenticate() {
    }

    private void setUpDefaultStore() {
        jdbcTemplate.update("""
                MERGE INTO store (id, name) KEY(id)
                VALUES (1, '잠실점')
                """);
    }

    private void setUpDefaultStoreManager(String loginId) {
        Long memberId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM member
                WHERE login_id = ?
                """, Long.class, loginId);

        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM store_manager
                WHERE member_id = ?
                """, Integer.class, memberId);

        if (count == null || count == 0) {
            jdbcTemplate.update("""
                    INSERT INTO store_manager (member_id, store_id)
                    VALUES (?, 1)
                    """, memberId);
        }
    }

    @AfterEach
    void tearDownAcceptanceTest() {
        RestAssured.reset();
    }
}
