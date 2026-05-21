package roomescape.reservationtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.infra.dto.ReservationTimeAvailability;
import roomescape.common.exception.DomainException;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.store.domain.Store;
import roomescape.theme.domain.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;
import static roomescape.theme.exception.ThemeErrorCode.*;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationTime create(Long storeId, LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(new Store(storeId), startAt);
        validateNotDuplicated(reservationTime);
        try {
            return reservationTimeRepository.save(reservationTime);
        } catch (DataIntegrityViolationException e) {
            throw new DomainException(RESERVATION_TIME_ALREADY_EXISTS);
        }
    }

    private void validateNotDuplicated(ReservationTime reservationTime) {
        if (reservationTimeRepository.existsByStoreIdAndStartAt(
                reservationTime.getStore().getId(),
                reservationTime.getStartAt()
        )) {
            throw new DomainException(RESERVATION_TIME_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        if (reservationRepository.existByTimeId(id)) {
            throw new DomainException(RESERVATION_TIME_HAS_RESERVATION);
        }

        if (!reservationTimeRepository.cancelById(id)) {
            throw new DomainException(RESERVATION_TIME_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeAvailability> findAvailableTimes(LocalDate date, Long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new DomainException(THEME_NOT_FOUND);
        }

        return reservationTimeRepository.findAllByDateAndThemeIdWithAvailability(date, themeId);
    }
}
