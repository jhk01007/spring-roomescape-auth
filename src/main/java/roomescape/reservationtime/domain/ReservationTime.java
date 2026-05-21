package roomescape.reservationtime.domain;

import roomescape.common.exception.DomainException;
import roomescape.store.domain.Store;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;

public class ReservationTime {
    private final Long id;
    private final Store store;
    private final LocalTime startAt;
    private final LocalDateTime deletedAt;

    public ReservationTime(Long id, Store store, LocalTime startAt) {
        this(id, store, startAt, null);
    }

    public ReservationTime(Long id, Store store, LocalTime startAt, LocalDateTime deletedAt) {
        validateStore(store);
        validateStartAt(startAt);
        this.id = id;
        this.store = store;
        this.startAt = startAt;
        this.deletedAt = deletedAt;
    }

    public ReservationTime(Store store, LocalTime startAt) {
        this(null, store, startAt);
    }

    public ReservationTime withId(Long id) {
        validateId(id);
        if (this.id != null) {
            throw new DomainException(RESERVATION_TIME_ALREADY_HAS_ID);
        }

        return new ReservationTime(id, store, startAt, deletedAt);
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new DomainException(INVALID_RESERVATION_TIME_ID);
        }
    }

    private void validateStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new DomainException(INVALID_RESERVATION_TIME);
        }
    }

    private void validateStore(Store store) {
        if (store == null || store.getId() == null) {
            throw new DomainException(INVALID_RESERVATION_TIME_STORE);
        }
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationTime that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
