package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.validator.ReservationValidator;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.common.exception.DomainException;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.store_manager.domain.StoreManager;
import roomescape.store_manager.domain.repository.StoreManagerRepository;
import roomescape.theme.domain.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

import static roomescape.auth.exception.AuthErrorCode.AUTHORIZATION_ERROR;
import static roomescape.reservation.exception.ReservationErrorCode.*;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;
import static roomescape.theme.exception.ThemeErrorCode.*;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final StoreManagerRepository storeManagerRepository;

    private final ReservationValidator reservationValidator;

    @Transactional
    public Reservation create(LocalDate date, Long timeId, Long themeId, Member guest) {
        ReservationTime time = getReservationTime(timeId);
        Theme theme = getTheme(themeId);

        Reservation reservation = new Reservation(theme.getStore(), guest, date, time, theme);

        reservationValidator.validateCreate(reservation);

        try {
            return reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException e) {
            throw new DomainException(RESERVATION_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllReservations(int page, int size) {
        return reservationRepository.findAll(page, size);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findManagedStoreReservations(Member manager, int page, int size) {
        StoreManager storeManager = getStoreManager(manager);
        return reservationRepository.findAllByStoreId(storeManager.getStore().getId(), page, size);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByGuest(Member guest) {
        return reservationRepository.findByGuestId(guest.getId());
    }

    @Transactional
    public Reservation editDateTime(Long reservationId, LocalDate date, Long timeId, Member requester) {
        Reservation reservation = getReservation(reservationId);
        ReservationTime changedTime = getReservationTime(timeId);
        Reservation changedReservation = reservation.changeDateAndTime(date, changedTime);

        reservationValidator.validateEdit(reservation, changedReservation, requester);

        updateReservation(changedReservation);

        return changedReservation;
    }

    @Transactional
    public Reservation editManagedStoreReservation(Long reservationId, LocalDate date, Long timeId, Member manager) {
        StoreManager storeManager = getStoreManager(manager);
        Reservation reservation = getReservation(reservationId);
        validateManagedStore(storeManager, reservation);

        ReservationTime changedTime = getReservationTime(timeId);
        Reservation changedReservation = reservation.changeDateAndTime(date, changedTime);

        reservationValidator.validateAdminEdit(reservation, changedReservation);

        updateReservation(changedReservation);

        return changedReservation;
    }

    @Transactional
    public void cancel(Long id) {
        cancelReservation(id);
    }

    @Transactional
    public void cancelManagedStoreReservation(Long reservationId, Member manager) {
        StoreManager storeManager = getStoreManager(manager);
        Reservation reservation = getReservation(reservationId);
        validateManagedStore(storeManager, reservation);
        cancelReservation(reservationId);
    }

    @Transactional
    public void deleteMine(Long id, Member requester) {
        Reservation reservation = getReservation(id);
        reservationValidator.validateDelete(reservation, requester);
        cancelReservation(id);
    }

    private void cancelReservation(Long id) {
        if(!reservationRepository.cancelById(id)) { // 위에서 NOT_FOUND를 검증하긴 하지만, 삭제 과정 중에 다른 사람이 변경할 수도 있기에 이중으로 검증
            throw new DomainException(RESERVATION_NOT_FOUND);
        }
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new DomainException(THEME_NOT_FOUND));
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new DomainException(RESERVATION_NOT_FOUND));
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new DomainException(RESERVATION_TIME_NOT_FOUND));
    }

    private StoreManager getStoreManager(Member manager) {
        return storeManagerRepository.findByMemberId(manager.getId())
                .orElseThrow(() -> new DomainException(AUTHORIZATION_ERROR));
    }

    private void validateManagedStore(StoreManager storeManager, Reservation reservation) {
        if (!storeManager.getStore().equals(reservation.getStore())) {
            throw new DomainException(AUTHORIZATION_ERROR);
        }
    }

    private void updateReservation(Reservation reservation) {
        if (!reservationRepository.updateDateAndTime(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getId()
        )) {
            throw new DomainException(RESERVATION_NOT_FOUND);
        }
    }

}
