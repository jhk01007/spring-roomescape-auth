package roomescape.store_manager.domain;

import roomescape.member.domain.Member;
import roomescape.store.domain.Store;

public class StoreManager {

    private final Long id;
    private final Member member;
    private final Store store;

    public StoreManager(Long id, Member member, Store store) {
        this.id = id;
        this.member = member;
        this.store = store;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Store getStore() {
        return store;
    }

}
