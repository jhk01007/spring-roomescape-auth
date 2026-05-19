package roomescape.member.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Role;

import java.sql.PreparedStatement;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcMemberRepository implements MemberRepository {

    private final RowMapper<Member> memberRowMapper = (resultSet, rowNum) ->
            Member.of(
                    resultSet.getLong("id"),
                    resultSet.getString("login_id"),
                    resultSet.getString("password"),
                    resultSet.getString("nickname"),
                    Role.valueOf(resultSet.getString("role"))
            );

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
    public Optional<Member> findByLoginId(String loginId) {
        return jdbcTemplate.query("""
                        SELECT id, nickname, login_id, password, role
                        FROM member
                        WHERE login_id = ?
                        """, memberRowMapper, loginId)
                .stream()
                .findFirst();
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
