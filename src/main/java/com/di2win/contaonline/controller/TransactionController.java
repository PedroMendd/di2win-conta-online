package com.di2win.contaonline.controller;

import com.di2win.contaonline.entity.Transaction;
import com.di2win.contaonline.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<Transaction> deposit(@PathVariable Long accountId, @RequestParam BigDecimal amount) {
        Transaction transaction = transactionService.deposit(accountId, amount);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<Transaction> withdraw(@PathVariable Long accountId, @RequestParam BigDecimal amount) {
        Transaction transaction = transactionService.withdraw(accountId, amount);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{accountId}/period")
    public ResponseEntity<List<Transaction>> getTransactionsByPeriod(@PathVariable Long accountId,
                                                                     @RequestParam LocalDateTime start,
                                                                     @RequestParam LocalDateTime end) {
        List<Transaction> transactions = transactionService.getTransactionsByPeriod(accountId, start, end);
        return ResponseEntity.ok(transactions);
    }
}
