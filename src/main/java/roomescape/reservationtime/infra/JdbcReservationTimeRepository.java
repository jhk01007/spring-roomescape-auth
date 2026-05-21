package roomescape.reservationtime.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.reservationtime.infra.dto.ReservationTimeAvailability;
import roomescape.store.domain.Store;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {


    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        insert(reservationTime, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return reservationTime.withId(id);
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query("""
                SELECT id, store_id, start_at, deleted_at
                FROM reservation_time
                WHERE deleted_at IS NULL
                """, reservationTimeRowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return jdbcTemplate.query("""
                        SELECT id, store_id, start_at, deleted_at
                        FROM reservation_time
                        WHERE id = ? AND deleted_at IS NULL
                        """, reservationTimeRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<ReservationTimeAvailability> findAllByDateAndThemeIdWithAvailability(LocalDate date, Long themeId) {
        String sql = """
                 SELECT rt.id AS id,
                        rt.store_id AS store_id,
                        rt.start_at AS start_at,
                        rt.deleted_at AS deleted_at,
                        r.id IS NULL AS available
                 FROM reservation_time rt
                 INNER JOIN theme th
                     ON th.id = ?
                    AND th.store_id = rt.store_id
                    AND th.deleted_at IS NULL
                 LEFT JOIN reservation r
                     ON r.time_id = rt.id
                    AND r.date = ?
                    AND r.theme_id = ?
                    AND r.deleted_at IS NULL
                WHERE rt.deleted_at IS NULL
                ORDER BY rt.start_at
                """;

        return jdbcTemplate.query(sql, reservationTimeAvailabilityRowMapper, themeId, date, themeId);
    }

    @Override
    public boolean existsByStoreIdAndStartAt(Long storeId, LocalTime startAt) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation_time
                WHERE store_id = ? AND start_at = ? AND deleted_at IS NULL
                """, Integer.class, storeId, startAt.toString());
        return count != null && count > 0;
    }

    @Override
    public boolean cancelById(Long id) {
        int rowCount = jdbcTemplate.update("""
                UPDATE reservation_time
                SET deleted_at = ?, delete_token = ?
                WHERE id = ? AND deleted_at IS NULL
                """, LocalDateTime.now(clock), id, id);
        return rowCount > 0;
    }

    private void insert(ReservationTime reservationTime, KeyHolder keyHolder) {
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            INSERT INTO reservation_time (store_id, start_at)
                            VALUES (?, ?)
                            """,
                    new String[]{"id"}
            );
            preparedStatement.setLong(1, reservationTime.getStore().getId());
            preparedStatement.setString(2, reservationTime.getStartAt().toString());
            return preparedStatement;
        }, keyHolder);
    }

    private final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    new Store(resultSet.getLong("store_id")),
                    LocalTime.parse(resultSet.getString("start_at")),
                    toLocalDateTime(resultSet.getTimestamp("deleted_at"))
            );

    private final RowMapper<ReservationTimeAvailability> reservationTimeAvailabilityRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("id"),
                new Store(resultSet.getLong("store_id")),
                LocalTime.parse(resultSet.getString("start_at")),
                toLocalDateTime(resultSet.getTimestamp("deleted_at"))
        );

        if(resultSet.getBoolean("available")) {
            return ReservationTimeAvailability.available(reservationTime);
        }
        return ReservationTimeAvailability.unavailable(reservationTime);
    };

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
