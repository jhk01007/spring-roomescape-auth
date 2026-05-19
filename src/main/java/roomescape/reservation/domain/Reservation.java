package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.common.exception.DomainException;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static roomescape.reservation.exception.ReservationErrorCode.*;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;
import static roomescape.theme.exception.ThemeErrorCode.*;

@Getter
public class Reservation {
    private final Long id;
    private final Member guest;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final LocalDateTime deletedAt;

    public Reservation(Long id, Member guest, LocalDate date, ReservationTime time, Theme theme, LocalDateTime deletedAt) {
        validateReservation(guest, date, time, theme);
        this.id = id;
        this.guest = guest;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.deletedAt = deletedAt;
    }

    public Reservation(Long id, Member guest, LocalDate date, ReservationTime time, Theme theme) {
        this(id, guest, date, time, theme, null);
    }

    public Reservation(Member guest, LocalDate date, ReservationTime time, Theme theme) {
        this(null, guest, date, time, theme);
    }

    private void validateReservation(Member guest, LocalDate date, ReservationTime time, Theme theme) {
        validateGuest(guest);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new DomainException(INVALID_RESERVATION_ID);
        }
    }

    private void validateGuest(Member member) {
        if (member == null) {
            throw new DomainException(INVALID_RESERVATION_GUEST);
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new DomainException(INVALID_RESERVATION_DATE);
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new DomainException(INVALID_RESERVATION_TIME);
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new DomainException(INVALID_THEME);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public boolean isPassed(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt())
                .isBefore(now);
    }

    public boolean isSameGuest(Member guest) {
        return Objects.equals(this.guest, guest);
    }

    public Reservation withId(Long id) {
        validateId(id);
        if (this.id != null) {
            throw new DomainException(RESERVATION_ALREADY_HAS_ID);
        }

        return new Reservation(id, guest, date, time, theme, deletedAt);
    }

    public Reservation changeDateAndTime(LocalDate changedDate, ReservationTime changedTime) {

        return new Reservation(
                id, guest, changedDate, changedTime, theme
        );
    }
}
