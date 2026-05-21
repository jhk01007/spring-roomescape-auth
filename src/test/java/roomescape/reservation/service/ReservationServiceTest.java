package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import roomescape.reservation.infra.JdbcReservationRepository;
import roomescape.reservation.service.validator.ReservationValidator;
import roomescape.reservationtime.infra.JdbcReservationTimeRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.store.domain.Store;
import roomescape.theme.infra.JdbcThemeRepository;
import roomescape.theme.domain.Theme;
import roomescape.test_config.clock.MutableClock;
import roomescape.test_config.clock.TestClockConfig;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;
import static roomescape.reservation.exception.ReservationErrorCode.*;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;

@JdbcTest
@Import({
        TestClockConfig.class,
        ReservationService.class,
        JdbcReservationRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class,
        ReservationValidator.class
})
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    MutableClock clock;


    @Test
    @DisplayName("이미 같은 날짜, 시간, 테마의 예약이 존재하면 예외가 발생한다.")
    public void create_fail1() {
        // given
        clock.setFixed(LocalDateTime.of(2026, 10, 10, 10, 0));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2026, 10, 11);
        Member guest = insertMember("브라운");
        insertReservation(guest, date, time, theme);

        // when, then
        assertThatThrownBy(() -> reservationService.create(date, time.getId(), theme.getId(), guest))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_ALREADY_EXISTS.message());
    }

    @Test
    @DisplayName("이미 지난 날짜 및 시간으로 예약하려는 경우 예외가 발생한다.")
    public void create_fail2() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate pastDate = LocalDate.of(2023, 8, 5);
        LocalDate currentDate = LocalDate.of(2025, 5, 11);

        clock.setFixed(currentDate);

        // when, then
        assertThatThrownBy(() -> reservationService.create(pastDate, time.getId(), theme.getId(), insertMember("포비")))
                .isInstanceOf(DomainException.class)
                .hasMessage(PAST_RESERVATION_NOT_ALLOWED.message());
    }

    @Test
    @DisplayName("해당 예약이 존재하지 않으면 취소할 수 없기 때문에 예외가 발생한다.")
    public void cancel_fail() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> reservationService.cancel(id))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("본인의 예약을 삭제한다.")
    public void cancelMine_success() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        Reservation reservation = insertReservation(
                insertMember("브라운"),
                LocalDate.of(2026, 10, 11),
                insertReservationTime(LocalTime.of(10, 0)),
                insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));

        // when
        reservationService.deleteMine(reservation.getId(), reservation.getGuest());

        // then
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE id = ? AND deleted_at IS NULL
                """, Integer.class, reservation.getId());
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("해당 예약이 존재하지 않으면 본인의 예약을 삭제할 수 없기 때문에 예외가 발생한다.")
    public void cancelMine_fail1() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> reservationService.deleteMine(id, createMockMember(1, "브라운")))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("이미 시작된 예약은 삭제할 수 없다.")
    public void cancelMine_fail2() {
        // given
        clock.setFixed(LocalDate.of(2023, 8, 11));

        Reservation reservation = insertReservation(
                insertMember("브라운"),
                LocalDate.of(2023, 8, 10),
                insertReservationTime(LocalTime.of(10, 0)),
                insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));

        // when, then
        assertThatThrownBy(() -> reservationService.deleteMine(reservation.getId(), reservation.getGuest()))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_ALREADY_STARTED_RESERVATION.message());
    }

    @Test
    @DisplayName("본인의 예약이 아니면 삭제할 수 없기 때문에 예외가 발생한다.")
    public void cancelMine_fail3() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        Member guest = insertMember("브라운");
        Reservation reservation = insertReservation(
                guest,
                LocalDate.of(2023, 8, 10),
                insertReservationTime(LocalTime.of(10, 0)),
                insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));

        // when, then
        assertThatThrownBy(() -> reservationService.deleteMine(reservation.getId(), createMockMember(guest.getId() + 1, "포비")))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_OTHER_GUEST_RESERVATION.message());
    }

    @Test
    @DisplayName("예약의 날짜 및 시간을 수정한다.")
    public void editDateTime_success() {
        // given
        Reservation reservation = insertReservation(
                insertMember("브라운"),
                LocalDate.of(2023, 8, 5),
                insertReservationTime(LocalTime.of(10, 0)),
                insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        clock.setFixed(LocalDate.of(2023, 7, 20));

        // when
        Reservation editedReservation =
                reservationService.editDateTime(reservation.getId(), editedDate, editedTime.getId(), reservation.getGuest());

        // then
        assertThat(editedReservation)
                .extracting(Reservation::getDate, r -> r.getTime().getId())
                .containsExactly(editedDate, editedTime.getId());
    }

    @Test
    @DisplayName("수정하려는 예약이 존재하지 않으면 예외가 발생한다.")
    public void editDateTime_fail1() {
        // given
        Long reservationId = 1L;
        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservationId, editedDate, editedTime.getId(), createMockMember(1L, "브라운")))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("수정하려는 예약 시간이 존재하지 않으면 예외가 발생한다.")
    public void editDateTime_fail2() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 20));

        Reservation reservation = insertReservation(
                insertMember("브라운"),
                LocalDate.of(2023, 8, 5),
                insertReservationTime(LocalTime.of(10, 0)),
                insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        Long editedTimeId = 999L;

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), editedDate, editedTimeId, reservation.getGuest()))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_TIME_NOT_FOUND.message());
    }

    @Test
    @DisplayName("이미 시작된 예약은 수정할 수 없다.")
    public void editDateTime_fail3() {
        // given
        clock.setFixed(LocalDate.of(2023, 8, 6));

        Reservation reservation = insertReservation(
                insertMember("브라운"),
                LocalDate.of(2023, 8, 5),
                insertReservationTime(LocalTime.of(10, 0)),
                insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), editedDate, editedTime.getId(), reservation.getGuest()))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_ALREADY_STARTED_RESERVATION.message());
    }

    @Test
    @DisplayName("수정하려는 날짜 및 시간에 예약이 존재하면 예외가 발생한다.")
    public void editDateTime_fail4() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation 수정할_예약 = insertReservation(
                insertMember("브라운"),
                LocalDate.of(2023, 8, 10),
                insertReservationTime(LocalTime.of(10, 0)),
                theme);

        Reservation 기존_예약 = insertReservation(
                insertMember("포비"),
                LocalDate.of(2023, 8, 6),
                insertReservationTime(LocalTime.of(12, 0)),
                theme);

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(수정할_예약.getId(), 기존_예약.getDate(), 기존_예약.getTime().getId(), 수정할_예약.getGuest()))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_ALREADY_EXISTS.message());
    }

    @Test
    @DisplayName("수정하려는 날짜 및 시간에 예약이 존재는 하는데 그게 본인의 예약인 경우 예외가 발생하지 않는다.")
    public void editDateTime_fail4_2() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        LocalDate date = LocalDate.of(2023, 8, 10);
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Reservation reservation = insertReservation(
                insertMember("브라운"),
                date,
                time,
                insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));

        // when then
        assertThatCode(() -> reservationService.editDateTime(reservation.getId(), date, time.getId(), reservation.getGuest()))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "2023-07-05, 10:00", // 날짜가 지난 경우
            "2023-07-06, 09:59", // 시간이 지난 경우
    })
    @DisplayName("이미 지난 날짜 및 시간으로 예약을 수정하려는 경우 예외가 발생한다.")
    public void editDateTime_fail5(LocalDate ed, LocalTime et) {
        // given
        clock.setFixed(LocalDateTime.of(2023, 7, 6, 10, 0));

        Reservation reservation = insertReservation(
                insertMember("브라운"),
                LocalDate.of(2023, 8, 6),
                insertReservationTime(LocalTime.of(12, 0)),
                insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));

        ReservationTime editedTime = insertReservationTime(et);

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), ed, editedTime.getId(), reservation.getGuest()))
                .isInstanceOf(DomainException.class)
                .hasMessage(PAST_RESERVATION_NOT_ALLOWED.message());
    }

    @Test
    @DisplayName("본인의 예약이 아니면 예외가 발생한다.")
    public void editDateTime_fail6() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Member guest = insertMember("브라운");
        Reservation reservation = insertReservation(
                guest,
                LocalDate.of(2023, 8, 10),
                time,
                insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(
                reservation.getId(), reservation.getDate(), time.getId(), createMockMember(guest.getId()+ 1, "other")))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_OTHER_GUEST_RESERVATION.message());
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
                MERGE INTO store (id, name) KEY(id)
                VALUES (1, '잠실점')
                """);
    }

    private Reservation insertReservation(Member guest, LocalDate date, ReservationTime time, Theme theme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation (store_id, guest_id, date, time_id, theme_id)
                    VALUES (?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setLong(1, theme.getStore().getId());
            preparedStatement.setLong(2, guest.getId());
            preparedStatement.setDate(3, Date.valueOf(date));
            preparedStatement.setLong(4, time.getId());
            preparedStatement.setLong(5, theme.getId());
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

    private Long getGeneratedId(KeyHolder keyHolder) {
        return keyHolder.getKey().longValue();
    }

    private static Member createMockMember(long id, String nickname) {
        return Member.of(id, "loginid1", Password.fromEncoded("password1"), nickname, Role.USER);
    }
}
