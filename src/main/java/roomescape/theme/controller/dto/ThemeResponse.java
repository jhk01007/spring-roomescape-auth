package roomescape.theme.controller.dto;

import roomescape.theme.domain.Theme;

public record ThemeResponse(Long id, Long storeId, String name, String description, String thumbnail) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getStore().getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }
}
