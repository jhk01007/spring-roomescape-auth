package roomescape.theme.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ThemeCreateRequest(
        @NotNull(message = "매장 id는 비어 있을 수 없습니다.")
        Long storeId,
        @NotBlank(message = "테마 이름은 비어 있을 수 없습니다.")
        String name,
        @NotBlank(message = "테마 설명은 비어 있을 수 없습니다.")
        String description,
        @NotBlank(message = "테마 썸네일은 비어 있을 수 없습니다.")
        String thumbnail
) {
}
