/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wad.controller;

import java.io.IOException;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wad.domain.Expense;
import wad.domain.ExpenseRow;
import wad.domain.Receipt;
import wad.domain.User;
import wad.repository.ExpenseRepository;
import wad.repository.ReceiptRepository;
import wad.service.ExpenseService;
import wad.service.ReceiptService;
import wad.service.UserService;
import wad.validator.ReceiptValidator;

/**
 *
 * @author teemu
 */
@Controller
@RequestMapping("/expenses/{expenseId}/receipts")
@SessionAttributes("expense")
public class ReceiptController {

    @Autowired
    @Qualifier("receiptValidator")
    ReceiptValidator receiptValidator;

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

    @RequestMapping(method = RequestMethod.POST)
    public String addReceipt(@RequestParam("file") MultipartFile file, @PathVariable Long expenseId,
            @ModelAttribute Receipt receipt, BindingResult bindingResult, 
            @ModelAttribute Expense expense, SessionStatus status,
            RedirectAttributes redirectAttrs) throws IOException {

        if (expense == null || !expense.isEditableBy(userService.getCurrentUser())) {
            throw new ResourceNotFoundException();
        }

        receipt.setName(file.getName());
        receipt.setMediaType(file.getContentType());
        receipt.setSize(file.getSize());
        receipt.setContent(file.getBytes());
        receipt.setSubmitted(new Date());
        receipt.setExpense(expense);

        receiptValidator.validate(receipt, bindingResult);

        if (bindingResult.hasErrors()) {
            redirectAttrs.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/expenses/" + expense.getId();
        }

        receiptRepository.save(receipt);

        status.setComplete();

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
