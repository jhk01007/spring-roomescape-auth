package roomescape.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import roomescape.auth.controller.dto.LoginRequest;
import roomescape.auth.service.AuthService;
import roomescape.member.domain.Member;
import roomescape.test_config.ControllerTest;

import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("로그인 요청을 한다.")
    public void login_success() throws Exception {
        // given
        String loginId = "jaehee123";
        String password = "password1";
        LoginRequest request = new LoginRequest(loginId, password);
        given(authService.login(loginId, password))
                .willReturn(Member.user(loginId, password, "jaehee"));

        // when then
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        then(authService).should()
                .login(loginId, password);
    }

    @ParameterizedTest
    @MethodSource("invalidLoginRequests")
    @DisplayName("로그인 요청 바디에 필수값이 누락되면 실패한다.")
    public void login_fail1(LoginRequest request) throws Exception {
        // when then
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private static Stream<LoginRequest> invalidLoginRequests() {
        return Stream.of(
                new LoginRequest(null, "password1"),
                new LoginRequest("jaehee123", null)
        );
    }
}
