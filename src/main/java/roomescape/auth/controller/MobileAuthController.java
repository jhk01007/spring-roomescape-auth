package roomescape.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.controller.dto.LoginRequest;
import roomescape.auth.controller.dto.TokenLoginResponse;
import roomescape.auth.infra.JwtProvider;
import roomescape.auth.service.AuthService;
import roomescape.member.domain.Member;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/mobile")
public class MobileAuthController {
    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<TokenLoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        Member member = authService.login(loginRequest.loginId(), loginRequest.password());
        String token = jwtProvider.createToken(member.getId());
        return ResponseEntity.ok().body(new TokenLoginResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
