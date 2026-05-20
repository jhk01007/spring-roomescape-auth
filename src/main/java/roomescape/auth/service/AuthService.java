package roomescape.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.PasswordEncoder;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberErrorCode;
import roomescape.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member login(String loginId, String rawPassword) {

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new DomainException(MemberErrorCode.INVALID_LOGIN_CREDENTIALS));

        if(!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new DomainException(MemberErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        return member;
    }
}
