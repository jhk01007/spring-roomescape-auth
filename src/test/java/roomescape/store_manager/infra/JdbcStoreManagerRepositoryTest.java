package roomescape.store_manager.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.member.domain.vo.Role;
import roomescape.store_manager.domain.StoreManager;
import roomescape.store_manager.domain.repository.StoreManagerRepository;

import java.sql.PreparedStatement;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcStoreManagerRepository.class)
class JdbcStoreManagerRepositoryTest {

    @Autowired
    private StoreManagerRepository storeManagerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("회원 id로 매장 매니저 정보를 조회한다.")
    void findByMemberId() {
        // given
        Long memberId = insertMember("manager1", "매니저");
        insertStore(1L, "잠실점");
        Long storeManagerId = insertStoreManager(memberId, 1L);

        // when
        Optional<StoreManager> storeManager = storeManagerRepository.findByMemberId(memberId);

        // then
        assertThat(storeManager).isPresent();
        assertThat(storeManager.get()).extracting(
                StoreManager::getId,
                manager -> manager.getMember().getId(),
                manager -> manager.getStore().getId(),
                manager -> manager.getStore().getName()
        ).containsExactly(storeManagerId, memberId, 1L, "잠실점");
    }

    @Test
    @DisplayName("매장 매니저가 아닌 회원 id로 조회하면 빈 값을 반환한다.")
    void findByMemberId_empty() {
        // given
        Long memberId = insertMember("user1", "사용자");

        // when
        Optional<StoreManager> storeManager = storeManagerRepository.findByMemberId(memberId);

        // then
        assertThat(storeManager).isEmpty();
    }

    private Long insertMember(String loginId, String nickname) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO member (nickname, login_id, password, role)
                    VALUES (?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, nickname);
            preparedStatement.setString(2, loginId);
            preparedStatement.setString(3, "password1");
            preparedStatement.setString(4, Role.USER.name());
            return preparedStatement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    private void insertStore(Long id, String name) {
        jdbcTemplate.update("""
                INSERT INTO store (id, name)
                VALUES (?, ?)
                """, id, name);
    }

    private Long insertStoreManager(Long memberId, Long storeId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO store_manager (member_id, store_id)
                    VALUES (?, ?)
                    """, new String[]{"id"});
            preparedStatement.setLong(1, memberId);
            preparedStatement.setLong(2, storeId);
            return preparedStatement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}
