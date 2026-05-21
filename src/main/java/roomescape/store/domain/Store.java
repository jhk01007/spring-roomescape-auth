package roomescape.store.domain;

import java.util.Objects;

public class Store {

    private final Long id;

    public Store(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Store store)) return false;
        return id != null && Objects.equals(id, store.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
