package roomescape.store_manager.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Password;
import roomescape.member.domain.vo.Role;
import roomescape.store.domain.Store;
import roomescape.store_manager.domain.StoreManager;
import roomescape.store_manager.service.StoreManagerService;
import roomescape.test_config.web.ControllerTest;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(StoreManagerController.class)
class StoreManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoreManagerService storeManagerService;

    @Test
    @DisplayName("내 매장 매니저 정보를 조회한다.")
    void getMine_manager() throws Exception {
        // given
        StoreManager storeManager = new StoreManager(1L, member(), new Store(1L, "잠실점"));
        given(storeManagerService.findMine(any(Member.class)))
                .willReturn(Optional.of(storeManager));

        // when then
        mockMvc.perform(get("/store-managers/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manager").value(true))
                .andExpect(jsonPath("$.store.id").value(1))
                .andExpect(jsonPath("$.store.name").value("잠실점"));

        then(storeManagerService).should().findMine(any(Member.class));
    }

    @Test
    @DisplayName("매장 매니저가 아니면 store를 null로 응답한다.")
    void getMine_notManager() throws Exception {
        // given
        given(storeManagerService.findMine(any(Member.class)))
                .willReturn(Optional.empty());

        // when then
        mockMvc.perform(get("/store-managers/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manager").value(false))
                .andExpect(jsonPath("$.store").doesNotExist());

        then(storeManagerService).should().findMine(any(Member.class));
    }

    private Member member() {
        return Member.of(1L, "manager1", Password.fromEncoded("password1"), "매니저", Role.USER);
    }
}
