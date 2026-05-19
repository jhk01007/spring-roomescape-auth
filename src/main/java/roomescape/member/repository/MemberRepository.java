package roomescape.member.repository;

import roomescape.member.domain.Member;

public interface MemberRepository {
    Member save(Member member);

    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);
}
