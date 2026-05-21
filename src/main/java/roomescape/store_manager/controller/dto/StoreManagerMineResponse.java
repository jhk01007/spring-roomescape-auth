package roomescape.store_manager.controller.dto;

import roomescape.store_manager.domain.StoreManager;

import java.util.Optional;

public record StoreManagerMineResponse(
        boolean manager,
        ManagedStoreResponse store
) {

    public static StoreManagerMineResponse from(Optional<StoreManager> storeManager) {
        return storeManager
                .map(manager -> new StoreManagerMineResponse(true, ManagedStoreResponse.from(manager.getStore())))
                .orElseGet(() -> new StoreManagerMineResponse(false, null));
    }
}
