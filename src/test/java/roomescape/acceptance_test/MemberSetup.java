package roomescape.acceptance_test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import roomescape.auth.controller.dto.LoginRequest;
import roomescape.member.controller.dto.MemberCreateRequest;

import static io.restassured.RestAssured.given;


public class MemberSetup {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void signUp(String loginId, String password, String nickname) throws JsonProcessingException {
        MemberCreateRequest request = new MemberCreateRequest(loginId, password, nickname);

        given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/members")
                .then().log().all()
                .statusCode(201);
    }

    public static void login(String loginId, String password) throws JsonProcessingException {
        LoginRequest request = new LoginRequest(loginId, password);

        given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/auth/login")
                .then().log().all()
                .statusCode(204);
    }
}
