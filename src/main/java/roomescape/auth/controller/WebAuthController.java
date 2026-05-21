package roomescape.auth.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.controller.dto.MemberLoginRequest;
import roomescape.auth.domain.LoginMemberInfo;
import roomescape.auth.service.AuthService;
import roomescape.member.domain.Member;

import static roomescape.auth.domain.LoginMemberInfo.LOGIN_MEMBER_INFO;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/web")
public class WebAuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid MemberLoginRequest memberLoginRequest, HttpSession httpSession) {
        Member member = authService.login(memberLoginRequest.loginId(), memberLoginRequest.password());
        httpSession.setAttribute(LOGIN_MEMBER_INFO, LoginMemberInfo.from(member));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession httpSession) {
        httpSession.invalidate();
        return ResponseEntity.noContent().build();
    }
}
