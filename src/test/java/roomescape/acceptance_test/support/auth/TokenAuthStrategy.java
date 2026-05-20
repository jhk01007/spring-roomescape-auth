package roomescape.acceptance_test.support.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import roomescape.auth.controller.dto.LoginRequest;

import static io.restassured.RestAssured.given;
import static roomescape.acceptance_test.support.auth.MemberSetup.signUp;
import static roomescape.auth.interceptor.JwtAuthInterceptor.AUTHORIZATION_HEADER;
import static roomescape.auth.interceptor.JwtAuthInterceptor.AUTHORIZATION_PREFIX;

public class TokenAuthStrategy implements AuthStrategy {
    @Override
    public void authenticate(String loginId, String password, String nickname) throws JsonProcessingException {
        signUp(loginId, password, "test");
        String token = tokenLogin(loginId, password);

        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addHeader(AUTHORIZATION_HEADER, AUTHORIZATION_PREFIX + token)
                .setContentType(ContentType.JSON)
                .build();
    }

    private String tokenLogin(String loginId, String password) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(new LoginRequest(loginId, password)))
                .when()
                .post("/auth/mobile/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("token");
    }
}
