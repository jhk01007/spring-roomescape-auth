package roomescape.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.annotation.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.PageRequest;
import roomescape.reservation.controller.dto.ReservationEditRequest;
import roomescape.reservation.controller.dto.ReservationListResponse;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {
    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<ReservationListResponse> getAllReservations(
            @LoginMember Member manager,
            @ModelAttribute @Valid PageRequest pageRequest
    ) {
        return ResponseEntity.ok(
                ReservationListResponse.from(reservationService.findManagedStoreReservations(manager, pageRequest.page(), pageRequest.size())
                        .stream()
                        .map(ReservationResponse::from)
                        .toList()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> edit(
            @PathVariable Long id,
            @RequestBody @Valid ReservationEditRequest request,
            @LoginMember Member manager
    ) {
        return ResponseEntity.ok(
                ReservationResponse.from(
                        reservationService.editManagedStoreReservation(id, request.date(), request.timeId(), manager)
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @LoginMember Member manager
    ) {
        reservationService.cancelManagedStoreReservation(id, manager);
        return ResponseEntity.noContent().build();
    }
}
