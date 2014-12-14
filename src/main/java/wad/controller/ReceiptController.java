/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wad.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import wad.domain.Expense;
import wad.domain.Receipt;
import wad.domain.User;
import wad.repository.ExpenseRepository;
import wad.repository.ReceiptRepository;
import wad.service.ExpenseService;
import wad.service.ReceiptService;
import wad.service.UserService;

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

    @Autowired
    private UserService userService;
    
    @Autowired
    private ExpenseService expenseService;
    
    @Autowired
    private ReceiptService receiptService;

    @ModelAttribute("receipt")
    private Receipt getReceipt() {
        return new Receipt();
    }

    @RequestMapping(value = "/{expenseId}", method = RequestMethod.GET)
    public String listReceipts(@PathVariable Long expenseId, Model model) {
        Expense expense = expenseService.getExpense(expenseId);
        User currentUser = userService.getCurrentUser();

        if (expense == null || !expense.isViewableBy(currentUser)) {
            throw new ResourceNotFoundException();
        }
        
        List<Receipt> list = expense.getReceipts();
        model.addAttribute("receipts", list);
        
        return "/expenses/" + expense.getId();
    }
    
    // Remember to add receipt name checking (no two receipts of same name).
    @RequestMapping(method = RequestMethod.POST)
    public String addReceipt(@RequestParam("file") MultipartFile file, @PathVariable Long expenseId) throws IOException {
        Receipt receipt = new Receipt();
        Expense expense = expenseRepository.findOne(expenseId);

        //expense.getReceipts().add(receipt);

        if (receiptService.checkReceiptName(file.getName())) {
            return "redirect:/expenses/" + expense.getId();
        } else {
                    receipt.setName(file.getName());
        receipt.setMediaType(file.getContentType());
        receipt.setSize(file.getSize());
        receipt.setContent(file.getBytes());
        receipt.setSubmitted(new Date());
        receipt.setExpense(expense);
        
        receiptRepository.save(receipt);
        }
        //expenseRepository.save(expense);

        return "redirect:/expenses/" + expense.getId();
    }

    @RequestMapping(value = "/{receiptId}/delete", method = RequestMethod.POST)
    public String deleteReceipt(@PathVariable Long receiptId, @PathVariable Long expenseId) {
        Expense expense = expenseRepository.findOne(expenseId);
        Receipt receipt = receiptRepository.findOne(receiptId);

        User currentUser = userService.getCurrentUser();

        if (expense == null || !expense.isEditableBy(currentUser)) {
            throw new ResourceNotFoundException();
        }

        expense.getReceipts().remove(receipt);
        receiptRepository.delete(receipt);

        return "redirect:/expenses/" + expense.getId();
    }
}
