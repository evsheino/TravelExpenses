package wad.domain;

import java.util.Date;
import java.util.List;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.format.annotation.DateTimeFormat;

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

    @NotBlank
    private String description;

    @NotNull
    private Status status;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Column(name = "expense_start_date")
    @NotNull
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Column(name = "expense_end_date")
    @NotNull
    private Date endDate;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="user_id")
    @NotNull
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy hh:mm:ss")
    @Column(name = "expense_modified_date")
    @NotNull
    private Date modified;

    @OneToMany(mappedBy = "expense", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ExpenseRow> expenseRows;

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<ExpenseRow> getExpenseRows() {
        return expenseRows;
    }

    public void setExpenseRows(List<ExpenseRow> expenseRows) {
        this.expenseRows = expenseRows;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(User supervisor) {
        this.supervisor = supervisor;
    }
}
