package roomescape.acceptance_test.support.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import roomescape.member.controller.dto.MemberCreateRequest;

import static io.restassured.RestAssured.given;


public class MemberSetup {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void signUp(String loginId, String password, String nickname) throws JsonProcessingException {
        signUp("/members", loginId, password, nickname);
    }

    public static void adminSignUp(String loginId, String password, String nickname) throws JsonProcessingException {
        signUp("/members/admin", loginId, password, nickname);
    }

    private static void signUp(
            String path,
            String loginId,
            String password,
            String nickname
    ) throws JsonProcessingException {
        MemberCreateRequest request = new MemberCreateRequest(loginId, password, nickname);

        given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post(path)
                .then().log().all()
                .statusCode(201);
    }
}
