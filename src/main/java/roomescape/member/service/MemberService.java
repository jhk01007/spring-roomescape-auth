package roomescape.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.PasswordEncoder;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Password;
import roomescape.member.exception.MemberErrorCode;
import roomescape.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member signUp(String loginId, String rawPassword, String nickname) {

        Password password = Password.encode(rawPassword, passwordEncoder);
        Member member = Member.user(loginId, password, nickname);
        validateMemberCanCreate(loginId, nickname);
        try {
            return memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            throw convertDuplicateException(e);
        }
    }

    private void validateMemberCanCreate(String loginId, String nickname) {
        if (memberRepository.existsByLoginId(loginId)) {
            throw new DomainException(MemberErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new DomainException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    private DomainException convertDuplicateException(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();

        if (message.contains("UK_MEMBER_LOGIN_ID")) {
            return new DomainException(MemberErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }
        return new DomainException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);

    }
}
