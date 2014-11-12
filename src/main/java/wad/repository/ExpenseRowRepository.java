package wad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wad.domain.Expense;
import wad.domain.ExpenseRow;

import java.util.List;

public interface ExpenseRowRepository extends JpaRepository<ExpenseRow, Long>{
    public List<ExpenseRow> findByExpense(Expense expense);
}
