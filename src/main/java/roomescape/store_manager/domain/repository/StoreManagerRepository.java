package roomescape.store_manager.domain.repository;

import roomescape.store_manager.domain.StoreManager;

import java.util.Optional;

public interface StoreManagerRepository {

    Optional<StoreManager> findByMemberId(Long memberId);
}
