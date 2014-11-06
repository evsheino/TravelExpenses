package wad.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Entity
@Table(indexes={@Index(columnList="user_id, authority", unique = true)})
public class Authority extends AbstractPersistable<Long> implements GrantedAuthority {

    public static enum Auth {
        USER,
        SUPERVISOR,
        ADMIN
    }

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
    @Enumerated(EnumType.STRING)
    private Auth authority;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String getAuthority() {
        return authority.toString();
    }

    public void setAuthority(Auth authority) {
        this.authority = authority;
    }

}
