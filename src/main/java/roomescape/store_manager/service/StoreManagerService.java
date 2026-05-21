package roomescape.store_manager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.store_manager.domain.StoreManager;
import roomescape.store_manager.domain.repository.StoreManagerRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreManagerService {

    private final StoreManagerRepository storeManagerRepository;

    @Transactional(readOnly = true)
    public Optional<StoreManager> findMine(Member member) {
        return storeManagerRepository.findByMemberId(member.getId());
    }
}
