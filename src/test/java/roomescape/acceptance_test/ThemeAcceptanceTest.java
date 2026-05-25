package roomescape.acceptance_test;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.acceptance_test.support.AcceptanceTestSupport;
import roomescape.reservation.controller.dto.ReservationCreateRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.test_config.clock.MutableClock;
import roomescape.theme.controller.dto.ThemeCreateRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ThemeAcceptanceTest extends AcceptanceTestSupport {

    @Autowired
    private MutableClock mutableClock;

    @Test
    @DisplayName("테마 생성 후 목록에서 조회된다.")
    public void scenario1() throws JsonProcessingException {
        ThemeCreateRequest request = new ThemeCreateRequest(1L, "brown", "설명", "섬네일");
        Integer themeId = createTheme(request);

        given().log().all()
        .when()
            .get("/themes")
        .then().log().all()
            .statusCode(200)
            .body("themes.id", hasItem(themeId))
            .body("themes.name", hasItem(request.name()))
            .body("themes.description", hasItem(request.description()))
            .body("themes.thumbnail", hasItem(request.thumbnail()));
    }

    @Test
    @DisplayName("테마 생성 후 삭제하면 목록에서 사라진다.")
    public void scenario2() throws JsonProcessingException {
        ThemeCreateRequest request = new ThemeCreateRequest(1L, "테마1", "설명", "섬네일");
        Integer themeId = createTheme(request);

        given().log().all()
                .pathParam("id", themeId)
                .when()
                .delete("/admin/themes/{id}")
                .then().log().all()
                .statusCode(204);

        given().log().all()
                .when()
                .get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.id", not(hasItem(themeId)));

    }

    @Test
    @DisplayName("인기 테마는 기간 내 예약 수가 많은 순서대로 조회된다.")
    public void scenario3() throws JsonProcessingException {
        LocalDate baseDate = LocalDate.of(2026, 5, 11);
        LocalDate date = baseDate.minusDays(1);
        LocalDate outOfRangeDate = baseDate.plusDays(1);

        mutableClock.setFixed(LocalDate.of(2026, 4, 1));

        Integer themeId = createTheme(new ThemeCreateRequest(1L, "인기 테마1", "설명", "섬네일"));
        Integer themeId2 = createTheme(new ThemeCreateRequest(1L, "인기 테마2", "설명", "섬네일"));
        Integer themeId3 = createTheme(new ThemeCreateRequest(1L, "인기 테마3", "설명", "섬네일"));
        Integer outOfRangeThemeId = createTheme(new ThemeCreateRequest(1L, "기간 밖 테마", "설명", "섬네일"));

        List<Integer> reservationTimeIds = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            reservationTimeIds.add(createReservationTime(new ReservationTimeCreateRequest(1L, LocalTime.of(i, 30))));
        }

        createReservations(date, reservationTimeIds, themeId, 13);
        createReservations(date, reservationTimeIds, themeId2, 12);
        createReservations(date, reservationTimeIds, themeId3, 11);
        createReservation(outOfRangeDate, reservationTimeIds.get(0), outOfRangeThemeId);

        mutableClock.setFixed(baseDate);

        given().log().all()
                .queryParam("days", 7)
                .queryParam("size", 3)
        .when()
                .get("/themes/popularity")
        .then().log().all()
                .statusCode(200)
                .body("themes", hasSize(3))
                .body("themes.id", contains(themeId, themeId2, themeId3))
                .body("themes.id", not(hasItem(outOfRangeThemeId)));
    }

    private Integer createTheme(ThemeCreateRequest request) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo(request.name()))
                .body("description", equalTo(request.description()))
                .body("thumbnail", equalTo(request.thumbnail()))
                .extract().path("id");
    }

    private Integer createReservationTime(ReservationTimeCreateRequest request) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", equalTo(request.startAt().toString()))
                .extract().path("id");
    }

    private void createReservations(
            LocalDate date,
            List<Integer> reservationTimeIds,
            Integer themeId,
            int count
    ) throws JsonProcessingException {
        for (int i = 0; i < count; i++) {
            createReservation(date, reservationTimeIds.get(i), themeId);
        }
    }

    private Integer createReservation(LocalDate date, Integer reservationTimeId, Integer themeId) throws JsonProcessingException {
        ReservationCreateRequest request = new ReservationCreateRequest(
                date,
                reservationTimeId.longValue(),
                themeId.longValue());

        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("date", equalTo(request.date().toString()))
                .body("time.id", equalTo(reservationTimeId))
                .body("theme.id", equalTo(themeId))
                .extract().path("id");
    }
}
