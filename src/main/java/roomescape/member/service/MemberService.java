package roomescape.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.PasswordEncoder;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Password;
import roomescape.member.domain.vo.Role;
import roomescape.member.exception.MemberErrorCode;
import roomescape.member.domain.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member signUp(String loginId, String rawPassword, String nickname) {
        return signUp(loginId, rawPassword, nickname, Role.USER);
    }

    public Member signUpAdmin(String loginId, String rawPassword, String nickname) {
        return signUp(loginId, rawPassword, nickname, Role.ADMIN);
    }

    private Member signUp(String loginId, String rawPassword, String nickname, Role role) {
        Password password = Password.encode(rawPassword, passwordEncoder);
        Member member = createMember(loginId, password, nickname, role);
        validateMemberCanCreate(loginId, nickname);
        try {
            return memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            throw convertDuplicateException(e);
        }
    }

    private Member createMember(String loginId, Password password, String nickname, Role role) {
        if (role == Role.ADMIN) {
            return Member.admin(loginId, password, nickname);
        }
        return Member.user(loginId, password, nickname);
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
