package com.di2win.contaonline.controller;

import com.di2win.contaonline.dto.AccountCreationDTO;
import com.di2win.contaonline.dto.AccountResponseDTO;
import com.di2win.contaonline.dto.DepositDTO;
import com.di2win.contaonline.dto.TransactionDTO;
import com.di2win.contaonline.dto.WithdrawalDTO;
import com.di2win.contaonline.service.AccountService;
import com.di2win.contaonline.util.AccountMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@RequestBody AccountCreationDTO accountCreationDTO) {
        var newAccount = accountService.createAccount(accountCreationDTO);
        AccountResponseDTO responseDTO = AccountMapper.mapToAccountResponseDTO(newAccount);
        return ResponseEntity.created(URI.create("/api/accounts/" + newAccount.getId())).body(responseDTO);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long accountId) {
        var account = accountService.findById(accountId);
        return ResponseEntity.ok(account.getSaldo());
    }

    @PutMapping("/{accountId}/deposit")
    public ResponseEntity<AccountResponseDTO> deposit(@PathVariable Long accountId, @RequestBody @Valid DepositDTO depositDTO) {
        var account = accountService.deposit(accountId, depositDTO.getAmount());
        AccountResponseDTO responseDTO = AccountMapper.mapToAccountResponseDTO(account);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{accountId}/withdraw")
    public ResponseEntity<AccountResponseDTO> withdraw(@PathVariable Long accountId, @RequestBody @Valid WithdrawalDTO withdrawalDTO) {
        var account = accountService.withdraw(accountId, withdrawalDTO.getAmount());
        AccountResponseDTO responseDTO = AccountMapper.mapToAccountResponseDTO(account);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByPeriod(
            @PathVariable Long accountId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("A data de início não pode ser posterior à data de término.");
        }

        List<TransactionDTO> transactions = accountService.getTransactionsByPeriod(accountId, start, end);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{accountId}/block")
    public ResponseEntity<Void> blockAccount(@PathVariable Long accountId) {
        accountService.blockAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{accountId}/unblock")
    public ResponseEntity<Void> unblockAccount(@PathVariable Long accountId) {
        accountService.unblockAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}
