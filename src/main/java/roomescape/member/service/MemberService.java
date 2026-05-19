package roomescape.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberErrorCode;
import roomescape.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public Member signUp(String loginId, String password, String nickname) {
        Member member = Member.user(loginId, password, nickname);
        validateMemberCanCreate(loginId, nickname);
        return memberRepository.save(member);
    }

    private void validateMemberCanCreate(String loginId, String nickname) {
        if(memberRepository.existsByLoginId(loginId)) {
            throw new DomainException(MemberErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }
        if(memberRepository.existsByNickname(nickname)) {
            throw new DomainException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }
}
