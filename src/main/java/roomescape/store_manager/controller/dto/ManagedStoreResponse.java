package roomescape.store_manager.controller.dto;

import roomescape.store.domain.Store;

public record ManagedStoreResponse(
        Long id,
        String name
) {

    public static ManagedStoreResponse from(Store store) {
        return new ManagedStoreResponse(store.getId(), store.getName());
    }
}
