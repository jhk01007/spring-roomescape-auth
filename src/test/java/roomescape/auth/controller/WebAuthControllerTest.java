package roomescape.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import roomescape.auth.controller.dto.LoginRequest;
import roomescape.auth.service.AuthService;
import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Password;
import roomescape.member.domain.vo.Role;
import roomescape.test_config.ControllerTest;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.auth.interceptor.SessionAuthInterceptor.LOGIN_MEMBER_ID;

@ControllerTest(WebAuthController.class)
class WebAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("로그인 요청을 하면 세션에 해당 사용자의 id정보가 세팅된다.")
    public void login_success() throws Exception {
        // given
        String loginId = "jaehee123";
        String password = "password1";
        LoginRequest request = new LoginRequest(loginId, password);
        Member mockMember = Member.of(1L, loginId, Password.fromEncoded(password), "jaehee", Role.USER);

        given(authService.login(loginId, password))
                .willReturn(mockMember);

        // when then
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/auth/web/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        // then
        HttpSession session = result.getRequest().getSession(false);

        assertThat(session).isNotNull();
        assertThat(session.getAttribute(LOGIN_MEMBER_ID)).isEqualTo(mockMember.getId());

        then(authService).should()
                .login(loginId, password);
    }

    @ParameterizedTest
    @MethodSource("invalidLoginRequests")
    @DisplayName("로그인 요청 바디에 필수값이 누락되면 실패한다.")
    public void login_fail1(LoginRequest request) throws Exception {
        // when then
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/auth/web/login")
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

    @Test
    @DisplayName("로그아웃을 한다.")
    public void logout_success() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(LOGIN_MEMBER_ID, 1L);

        // when
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/auth/web/logout")
                                .session(session)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        // then
        assertThat(session.isInvalid()).isTrue();
    }
}
