package roomescape.member.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.domain.vo.Password;
import roomescape.member.domain.vo.Role;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcMemberRepository.class)
class JdbcMemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("회원을 저장한다.")
    void save() {
        // given
        Member member = Member.user("login1", Password.fromEncoded("password1"), "닉네임");

        // when
        Member saved = memberRepository.save(member);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLoginId()).isEqualTo("login1");
        assertThat(saved.getPassword()).isEqualTo("password1");
        assertThat(saved.getNickname()).isEqualTo("닉네임");
        assertThat(saved.getRole()).isEqualTo(Role.USER);

        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT id, nickname, login_id, password, role
                FROM member
                WHERE id = ?
                """, saved.getId());
        assertThat(((Number) row.get("id")).longValue()).isEqualTo(saved.getId());
        assertThat(row.get("nickname")).isEqualTo("닉네임");
        assertThat(row.get("login_id")).isEqualTo("login1");
        assertThat(row.get("password")).isEqualTo("password1");
        assertThat(row.get("role")).isEqualTo(Role.USER.name());
    }

    @Test
    @DisplayName("id로 회원을 조회한다.")
    void findById() {
        // given
        Member saved = memberRepository.save(Member.user("login1", Password.fromEncoded("password1"), "닉네임"));

        // when
        Optional<Member> found = memberRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        Member member = found.get();
        assertThat(member)
                .extracting(
                        Member::getId,
                        Member::getLoginId,
                        Member::getPassword,
                        Member::getNickname,
                        Member::getRole
                )
                .containsExactly(saved.getId(), "login1", "password1", "닉네임", Role.USER);
    }

    @Test
    @DisplayName("존재하지 않는 id로 회원을 조회하면 빈 Optional을 반환한다.")
    void findById_empty() {
        // when
        Optional<Member> found = memberRepository.findById(1L);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("로그인 아이디로 회원을 조회한다.")
    void findByLoginId() {
        // given
        insertMember("login1", "password1", "닉네임");

        // when
        Optional<Member> found = memberRepository.findByLoginId("login1");

        // then
        assertThat(found).isPresent();
        Member member = found.get();
        assertThat(member)
                .extracting(
                        Member::getLoginId,
                        Member::getPassword,
                        Member::getNickname,
                        Member::getRole
                )
                .containsExactly("login1", "password1", "닉네임", Role.USER);
        assertThat(member.getId()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 로그인 아이디로 회원을 조회하면 빈 Optional을 반환한다.")
    void findByLoginId_empty() {
        // when
        Optional<Member> found = memberRepository.findByLoginId("login1");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("로그인 아이디 존재 여부를 조회한다.")
    void existsByLoginId() {
        // given
        insertMember("login1", "password1", "닉네임");

        // when
        boolean exists = memberRepository.existsByLoginId("login1");
        boolean notExists = memberRepository.existsByLoginId("login2");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("닉네임 존재 여부를 조회한다.")
    void existsByNickname() {
        // given
        insertMember("login1", "password1", "닉네임");

        // when
        boolean exists = memberRepository.existsByNickname("닉네임");
        boolean notExists = memberRepository.existsByNickname("다른닉네임");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    private void insertMember(String loginId, String password, String nickname) {
        jdbcTemplate.update("""
                INSERT INTO member (nickname, login_id, password, role)
                VALUES (?, ?, ?, ?)
                """, nickname, loginId, password, Role.USER.name());
    }
}
