package wad.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Entity
@Table(indexes={@Index(columnList="user_id, role", unique = true)})
public class Authority extends AbstractPersistable<Long> implements GrantedAuthority {

    public static enum Role {
        USER,
        SUPERVISOR,
        ADMIN
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Authority() {

    }

    public Authority(User user, Role authority) {
        this.user = user;
        this.role = authority;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String getAuthority() {
        return role.toString();
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isRole(Role role) {
        return this.role.equals(role);
    }

}
