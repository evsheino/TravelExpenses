package wad.domain;

import javax.persistence.*;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "app_user")
public class User extends AbstractPersistable<Long> {

    @NotBlank
    @Column(unique = true)
    private String username;

    @NotBlank
    private String name;

    @NotBlank
    @Length(min = 8)
    private String password;
    private String salt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Expense> expenses;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Authority> authorities;

    @OneToMany(mappedBy = "supervisor", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Expense> subordinatesExpenses;

    public User() {

    }

    public User(String name, String username, String password) {
        setName(name);
        setUsername(username);
        setPassword(password);
    }

    private boolean hasRole(Authority.Role role) {
        for (Authority auth : this.getAuthorities()) {
            if (auth.isRole(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin() {
        return hasRole(Authority.Role.ADMIN);
    }

    public boolean isSupervisor() {
        return hasRole(Authority.Role.SUPERVISOR);
    }

    public boolean isUser() {
        return hasRole(Authority.Role.USER);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.salt = BCrypt.gensalt();
        this.password = BCrypt.hashpw(password, this.salt);
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public List<Expense> getSubordinatesExpenses() {
        return subordinatesExpenses;
    }

    public void setSubordinatesExpenses(List<Expense> subordinatesExpenses) {
        this.subordinatesExpenses = subordinatesExpenses;
    }

}
