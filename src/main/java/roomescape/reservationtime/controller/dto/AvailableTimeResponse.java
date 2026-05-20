package roomescape.reservationtime.controller.dto;

import roomescape.reservationtime.infra.dto.ReservationTimeAvailability;

import java.time.LocalTime;

public record AvailableTimeResponse(
        Long id,
        LocalTime startAt,
        boolean isAvailable
) {

    public static AvailableTimeResponse from(ReservationTimeAvailability timeAvailability) {
        return new AvailableTimeResponse(
                timeAvailability.getReservationTime().getId(),
                timeAvailability.getReservationTime().getStartAt(),
                timeAvailability.isAvailable());
    }
}
