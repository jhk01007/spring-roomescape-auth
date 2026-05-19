package roomescape.auth.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.controller.dto.LoginRequest;
import roomescape.auth.service.AuthService;
import roomescape.member.domain.Member;

import static roomescape.auth.interceptor.AuthConst.LOGIN_MEMBER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest loginRequest, HttpSession httpSession) {
        Member member = authService.login(loginRequest.loginId(), loginRequest.password());
        httpSession.setAttribute(LOGIN_MEMBER_ID, member.getId());
        return ResponseEntity.noContent().build();
    }
}
