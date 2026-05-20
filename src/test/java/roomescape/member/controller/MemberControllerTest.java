package roomescape.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import roomescape.member.controller.dto.MemberCreateRequest;
import roomescape.member.controller.dto.MemberCreateResponse;
import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Password;
import roomescape.member.service.MemberService;
import roomescape.test_config.ControllerTest;

import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 요청을 한다.")
    public void createMember_success() throws Exception {
        // given
        String loginId = "jaehee123";
        String password = "password1";
        String nickname = "jay";
        Member mockMember = Member.user(loginId, Password.fromEncoded(password), nickname).withId(1L);

        given(memberService.signUp(loginId, password, nickname))
                .willReturn(mockMember);

        MemberCreateRequest request = new MemberCreateRequest(loginId, password, nickname);
        // when then
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MemberCreateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                MemberCreateResponse.class
        );
        Assertions.assertThat(response).extracting(
                MemberCreateResponse::id,
                MemberCreateResponse::loginId,
                MemberCreateResponse::nickname,
                MemberCreateResponse::role
        ).containsExactly(
                mockMember.getId(),
                mockMember.getLoginId(),
                mockMember.getNickname(),
                mockMember.getRole().name());

        then(memberService).should()
                .signUp(loginId, password, nickname);
    }

    @ParameterizedTest
    @MethodSource("invalidMemberCreateRequests")
    @DisplayName("회원가입 요청 바디에 필수값이 누락되면 실패한다.")
    public void createMember_fail1(MemberCreateRequest request) throws Exception {
        // when then
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private static Stream<MemberCreateRequest> invalidMemberCreateRequests() {
        return Stream.of(
                new MemberCreateRequest(null, "password1", "jay"),
                new MemberCreateRequest("jaehee123", null, "jay"),
                new MemberCreateRequest("jaehee123", "password1", null)
        );
    }
}
