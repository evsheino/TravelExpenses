package wad.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import wad.domain.Expense;
import wad.domain.User;

import java.util.List;

/**
 * @author teemu
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long>{

    public List<Expense> findByUser(User user);

    public Page<Expense> findAllByUser(User user, Pageable pageable);

    public Page<Expense> findAllByUserAndStatus(User user, Expense.Status status, Pageable pageable);

    public List<Expense> findExpensesByStatusOrderByModifiedAsc(Expense.Status status);

    public List<Expense> findExpensesByUserAndStatus(User user, Expense.Status status);

}
