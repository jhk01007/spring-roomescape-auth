package roomescape.acceptance_test.support.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import roomescape.auth.controller.dto.MemberLoginRequest;

import static io.restassured.RestAssured.given;
import static roomescape.acceptance_test.support.auth.MemberSetup.signUp;

public class WebAuthStrategy implements AuthStrategy {
    @Override
    public void authenticate(String loginId, String password, String nickname) throws JsonProcessingException {
        SessionFilter sessionFilter = new SessionFilter();
        RestAssured.filters(sessionFilter);

        signUp(loginId, password, nickname);
        sessionLogin(loginId, password);
    }

    private void sessionLogin(String loginId, String password) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(new MemberLoginRequest(loginId, password)))
                .when()
                .post("/auth/web/login")
                .then().log().all()
                .statusCode(204);
    }
}
