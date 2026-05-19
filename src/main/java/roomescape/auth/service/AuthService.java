package roomescape.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberErrorCode;
import roomescape.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;

    public void login(String loginId, String password) {

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new DomainException(MemberErrorCode.INVALID_LOGIN_CREDENTIALS));

        // TODO 비밀번호 암호화
        if(!member.isPasswordSame(password)) {
            throw new DomainException(MemberErrorCode.INVALID_LOGIN_CREDENTIALS);
        }
    }
}
