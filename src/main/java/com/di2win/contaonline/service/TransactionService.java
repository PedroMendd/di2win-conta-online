package com.di2win.contaonline.service;

import com.di2win.contaonline.entity.Account;
import com.di2win.contaonline.entity.Transaction;
import com.di2win.contaonline.entity.TransactionType;
import com.di2win.contaonline.exception.account.AccountNotFoundException;
import com.di2win.contaonline.repository.AccountRepository;
import com.di2win.contaonline.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    public Transaction deposit(Long accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);

        Transaction transaction = new Transaction();
        transaction.setConta(account);
        transaction.setValor(amount);
        transaction.setTipo(TransactionType.DEPOSITO);

        account.setSaldo(account.getSaldo().add(amount));
        accountRepository.save(account);

        return transactionRepository.save(transaction);
    }

    public Transaction withdraw(Long accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);

        if (account.getSaldo().compareTo(amount) < 0) {
            throw new RuntimeException("Saldo insuficiente!");
        }

        Transaction transaction = new Transaction();
        transaction.setConta(account);
        transaction.setValor(amount);
        transaction.setTipo(TransactionType.SAQUE);

        account.setSaldo(account.getSaldo().subtract(amount));
        accountRepository.save(account);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByPeriod(Long accountId, LocalDateTime start, LocalDateTime end) {
        Account account = findAccountById(accountId);
        return transactionRepository.findByContaAndDataHoraBetween(account, start, end);
    }

    private Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada: " + accountId));
    }
}
