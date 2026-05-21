package roomescape.store_manager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMember;
import roomescape.member.domain.Member;
import roomescape.store_manager.controller.dto.StoreManagerMineResponse;
import roomescape.store_manager.service.StoreManagerService;

@RestController
@RequestMapping("/store-managers")
@RequiredArgsConstructor
public class StoreManagerController {

    private final StoreManagerService storeManagerService;

    @GetMapping("/me")
    public ResponseEntity<StoreManagerMineResponse> getMine(@LoginMember Member member) {
        return ResponseEntity.ok(StoreManagerMineResponse.from(storeManagerService.findMine(member)));
    }
}
