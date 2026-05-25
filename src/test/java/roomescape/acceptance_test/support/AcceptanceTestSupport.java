package roomescape.acceptance_test.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.acceptance_test.support.auth.AuthStrategy;
import roomescape.acceptance_test.support.auth.TestAuthConfig;
import roomescape.test_config.clock.MutableClock;
import roomescape.test_config.clock.TestClockConfig;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestAuthConfig.class, TestClockConfig.class})
@Sql(value = "/acceptance-cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
public abstract class AcceptanceTestSupport {

    @LocalServerPort
    protected int port;

    @Autowired
    protected AuthStrategy authStrategy;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MutableClock mutableClock;

    @BeforeEach
    void setUpAcceptanceTest() throws JsonProcessingException {
        mutableClock.reset();
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
