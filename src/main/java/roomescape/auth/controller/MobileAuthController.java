package roomescape.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.controller.dto.*;
import roomescape.auth.service.AuthService;
import roomescape.auth.service.JwtService;
import roomescape.member.domain.Member;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/mobile")
public class MobileAuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid MemberLoginRequest memberLoginRequest) {
        Member member = authService.login(memberLoginRequest.loginId(), memberLoginRequest.password());
        return ResponseEntity.ok().body(TokenResponse.from(jwtService.issueToken(member)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid TokenLogoutRequest tokenLogoutRequest) {
        jwtService.logout(tokenLogoutRequest.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@RequestBody @Valid TokenRefreshRequest tokenRefreshRequest) {
        return ResponseEntity.ok()
                .body(new TokenRefreshResponse(
                        jwtService.refresh(tokenRefreshRequest.refreshToken())));
    }

}
