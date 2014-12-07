/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wad.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import wad.domain.Expense;
import wad.domain.Receipt;

/**
 *
 * @author teemu
 */
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    public List<Receipt> findByExpense (Expense expense);
}
