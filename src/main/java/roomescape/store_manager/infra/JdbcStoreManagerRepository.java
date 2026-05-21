package roomescape.store_manager.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Password;
import roomescape.member.domain.vo.Role;
import roomescape.store.domain.Store;
import roomescape.store_manager.domain.StoreManager;
import roomescape.store_manager.domain.repository.StoreManagerRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcStoreManagerRepository implements StoreManagerRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<StoreManager> findByMemberId(Long memberId) {
        return jdbcTemplate.query("""
                        SELECT
                            sm.id AS store_manager_id,
                            m.id AS member_id,
                            m.login_id,
                            m.password,
                            m.nickname,
                            m.role,
                            s.id AS store_id,
                            s.name AS store_name
                        FROM store_manager sm
                        INNER JOIN member m
                            ON sm.member_id = m.id
                        INNER JOIN store s
                            ON sm.store_id = s.id
                        WHERE sm.member_id = ?
                        """, storeManagerRowMapper, memberId)
                .stream()
                .findFirst();
    }

    private final RowMapper<StoreManager> storeManagerRowMapper = (resultSet, rowNum) -> {
        Member member = Member.of(
                resultSet.getLong("member_id"),
                resultSet.getString("login_id"),
                Password.fromEncoded(resultSet.getString("password")),
                resultSet.getString("nickname"),
                Role.valueOf(resultSet.getString("role"))
        );
        Store store = new Store(
                resultSet.getLong("store_id"),
                resultSet.getString("store_name")
        );
        return new StoreManager(resultSet.getLong("store_manager_id"), member, store);
    };
}
