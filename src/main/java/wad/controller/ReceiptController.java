/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wad.controller;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import wad.domain.Expense;
import wad.domain.Receipt;
import wad.repository.ExpenseRepository;
import wad.repository.ReceiptRepository;

/**
 *
 * @author teemu
 */
@Controller
@RequestMapping("/expenses/{expenseId}/receipts")
public class ReceiptController {

    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private ReceiptRepository receiptRepository;

    @ModelAttribute("receipt")
    private Receipt getReceipt() {
        return new Receipt();
    }

    @RequestMapping(method = RequestMethod.POST)
    public String addReceipt(@RequestParam("file") MultipartFile file, @PathVariable Long id) throws IOException {
        Receipt receipt = new Receipt();
        Expense expense = expenseRepository.findOne(id);

        receipt.setName(file.getName());
        receipt.setMediaType(file.getContentType());
        receipt.setSize(file.getSize());
        receipt.setContent(file.getBytes());
        
        List<Receipt> receipts = expense.getReceipts();
        receipts.add(receipt);
        expense.setReceipts(receipts);
        
        expenseRepository.save(expense);
        receiptRepository.save(receipt);

        return "redirect:/expenses/" + expense.getId();
    }
}
