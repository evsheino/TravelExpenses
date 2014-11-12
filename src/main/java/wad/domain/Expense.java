package wad.domain;

import javax.persistence.*;
import java.util.Date;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 *
 * @author teemu
 */

@Entity
public class Expense extends AbstractPersistable<Long> {

    public static enum Status {
        SAVED, // Saved, not sent yet to supervisor
        WAITING, // Sent to supervisor
        RETURNED, // Supervisor asks more info
        APPROVED // Supervisor approved expense
    }

    private double amount;
    private String description;

    @Temporal(TemporalType.DATE)
    @Column(name = "expense_date")
    private Date date;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Temporal(TemporalType.DATE)
    @Column(name = "expense_modified_date")
    private Date modified;

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
