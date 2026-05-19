package roomescape.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.controller.dto.MemberCreateDto;
import roomescape.member.service.MemberService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody @Valid MemberCreateDto memberCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(memberService.signUp(
                        memberCreateDto.loginId(), memberCreateDto.password(), memberCreateDto.password()));
    }
}
