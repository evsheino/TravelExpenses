package wad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wad.domain.Expense;

import java.util.List;
import wad.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>{
    public List<Comment> findByExpense(Expense expense);
}
