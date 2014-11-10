package wad.domain;

import javax.persistence.*;
import java.util.Date;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 *
 * @author teemu
 */

@Entity
public class Expense extends AbstractPersistable<Long> {
    
    private double amount;

    @Temporal(TemporalType.DATE)
    @Column(name = "expense_date")
    private Date date;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
