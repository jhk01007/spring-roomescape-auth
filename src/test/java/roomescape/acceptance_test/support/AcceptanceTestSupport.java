package roomescape.acceptance_test.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.acceptance_test.support.auth.TestAuthConfig;
import roomescape.auth.controller.dto.MemberLoginRequest;
import roomescape.test_config.clock.MutableClock;
import roomescape.test_config.clock.TestClockConfig;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static roomescape.acceptance_test.support.auth.MemberSetup.adminSignUp;
import static roomescape.acceptance_test.support.auth.MemberSetup.signUp;
import static roomescape.auth.interceptor.JwtAuthInterceptor.AUTHORIZATION_HEADER;
import static roomescape.auth.interceptor.JwtAuthInterceptor.AUTHORIZATION_PREFIX;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestAuthConfig.class, TestClockConfig.class})
@Sql(value = "/acceptance-cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
public abstract class AcceptanceTestSupport {

    protected static final String DEFAULT_ADMIN_LOGIN_ID = "test123";
    protected static final String DEFAULT_PASSWORD = "password1";
    protected static final String DEFAULT_ADMIN_NICKNAME = "test";

    @LocalServerPort
    protected int port;

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
        registerAndLoginAdmin(DEFAULT_ADMIN_LOGIN_ID, DEFAULT_PASSWORD, DEFAULT_ADMIN_NICKNAME);
    }

    protected void beforeAuthenticate() {
    }

    private void setUpDefaultStore() {
        jdbcTemplate.update("""
                MERGE INTO store (id, name) KEY(id)
                VALUES (1, '잠실점')
                """);
    }

    protected void registerAndLoginUser(String loginId, String password, String nickname) throws JsonProcessingException {
        clearAuthentication();
        signUp(loginId, password, nickname);
        loginAs(loginId, password);
    }

    protected void registerAndLoginAdmin(String loginId, String password, String nickname) throws JsonProcessingException {
        clearAuthentication();
        adminSignUp(loginId, password, nickname);
        loginAs(loginId, password);
        setUpDefaultStoreManager(loginId);
    }

    protected void loginAsDefaultAdmin() throws JsonProcessingException {
        loginAs(DEFAULT_ADMIN_LOGIN_ID, DEFAULT_PASSWORD);
    }

    protected void loginAs(String loginId, String password) throws JsonProcessingException {
        clearAuthentication();
        String token = tokenLogin(loginId, password);
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addHeader(AUTHORIZATION_HEADER, AUTHORIZATION_PREFIX + token)
                .setContentType(JSON)
                .build();
    }

    protected void clearAuthentication() {
        RestAssured.requestSpecification = null;
    }

    private String tokenLogin(String loginId, String password) throws JsonProcessingException {
        return given().log().all()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(new MemberLoginRequest(loginId, password)))
                .when()
                .post("/auth/mobile/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("accessToken");
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
