package roomescape.theme.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.Member;
import roomescape.member.domain.vo.Password;
import roomescape.member.domain.vo.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.store.domain.Store;
import roomescape.theme.domain.Theme;
import roomescape.reservation.infra.JdbcReservationRepository;
import roomescape.theme.infra.JdbcThemeRepository;
import roomescape.test_config.clock.TestClockConfig;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.theme.exception.ThemeErrorCode.*;

@JdbcTest
@Import({
        TestClockConfig.class,
        ThemeService.class,
        JdbcReservationRepository.class,
        JdbcThemeRepository.class
})
class ThemeServiceTest {

    @Autowired
    ThemeService themeService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("이미 예약 정보가 존재하는 테마는 삭제할 수 없다.")
    public void delete_fail() {
        // given
        ReservationTime reservationTime = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), reservationTime, theme);

        // when
        assertThatThrownBy(() -> themeService.delete(theme.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessage(THEME_HAS_RESERVATION.message());
    }

    @Test
    @DisplayName("해당 테마가 존재하지 않으면 삭제할 수 없기 때문에 예외가 발생한다.")
    public void delete_fail2() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> themeService.delete(id))
                .isInstanceOf(DomainException.class)
                .hasMessage(THEME_NOT_FOUND.message());
    }

    private Reservation insertReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        Member guest = insertMember(name);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation (guest_id, date, time_id, theme_id)
                    VALUES (?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setLong(1, guest.getId());
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, time.getId());
            preparedStatement.setLong(4, theme.getId());
            return preparedStatement;
        }, keyHolder);

        return new Reservation(getGeneratedId(keyHolder), guest, date, time, theme);
    }

    private Member insertMember(String nickname) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO member (nickname, login_id, password, role)
                    VALUES (?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, nickname);
            preparedStatement.setString(2, "login" + System.nanoTime());
            preparedStatement.setString(3, "password1");
            preparedStatement.setString(4, Role.USER.name());
            return preparedStatement;
        }, keyHolder);

        return Member.of(getGeneratedId(keyHolder), "login1", Password.fromEncoded("password1"), nickname, Role.USER);
    }

    private ReservationTime insertReservationTime(LocalTime startAt) {
        insertStore();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation_time (store_id, start_at)
                    VALUES (?, ?)
                    """, new String[]{"id"});
            preparedStatement.setLong(1, 1L);
            preparedStatement.setString(2, startAt.toString());
            return preparedStatement;
        }, keyHolder);

        return new ReservationTime(getGeneratedId(keyHolder), new Store(1L), startAt);
    }

    private Theme insertTheme(String name, String description, String thumbnail) {
        insertStore();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (store_id, name, description, thumbnail)
                    VALUES (?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setLong(1, 1L);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, description);
            preparedStatement.setString(4, thumbnail);
            return preparedStatement;
        }, keyHolder);

        return new Theme(getGeneratedId(keyHolder), new Store(1L), name, description, thumbnail);
    }

    private void insertStore() {
        jdbcTemplate.update("""
                MERGE INTO store KEY(id)
                VALUES (1)
                """);
    }

    private Long getGeneratedId(KeyHolder keyHolder) {
        return keyHolder.getKey().longValue();
    }
  
}
