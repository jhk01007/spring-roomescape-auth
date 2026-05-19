package roomescape.member.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;

import java.sql.PreparedStatement;

@Repository
@RequiredArgsConstructor
public class JdbcMemberRepository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Member save(Member member) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            INSERT INTO member (nickname, login_id, password, role)
                            VALUES (?, ?, ?, ?)
                            """,
                    new String[]{"id"}
            );
            preparedStatement.setString(1, member.getNickname());
            preparedStatement.setString(2, member.getLoginId());
            preparedStatement.setString(3, member.getPassword());
            preparedStatement.setString(4, member.getRole().name());
            return preparedStatement;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return member.withId(id);
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM member
                WHERE login_id = ?
                """, Integer.class, loginId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByNickname(String nickname) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM member
                WHERE nickname = ?
                """, Integer.class, nickname);
        return count != null && count > 0;
    }

}
