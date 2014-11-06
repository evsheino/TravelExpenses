package wad.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Entity
@Table(indexes={@Index(columnList="user_id, authority", unique = true)})
public class Authority extends AbstractPersistable<Long> implements GrantedAuthority {

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
    private String authority;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

}
