package com.di2win.contaonline.service;

import com.di2win.contaonline.entity.Account;
import com.di2win.contaonline.entity.Transaction;
import com.di2win.contaonline.entity.TransactionType;
import com.di2win.contaonline.exception.account.AccountBlockedException;
import com.di2win.contaonline.exception.account.InsufficientBalanceException;
import com.di2win.contaonline.exception.account.WithdrawalLimitExceededException;
import com.di2win.contaonline.repository.AccountRepository;
import com.di2win.contaonline.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeposit() {
        Long accountId = 1L;
        BigDecimal depositAmount = BigDecimal.valueOf(1000);


        Account account = new Account();
        account.setId(accountId);
        account.setSaldo(BigDecimal.valueOf(2000));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction transaction = transactionService.deposit(accountId, depositAmount);

        assertEquals(BigDecimal.valueOf(3000), account.getSaldo());
        assertEquals(TransactionType.DEPOSITO, transaction.getTipo());
        assertEquals(depositAmount, transaction.getValor());

        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testWithdrawSuccess() {
        Long accountId = 1L;
        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        BigDecimal withdrawAmount = BigDecimal.valueOf(200);

        Account account = new Account();
        account.setId(accountId);
        account.setSaldo(initialBalance);
        account.setBloqueada(false);
        account.setLimiteDiarioSaque(BigDecimal.valueOf(500));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setConta(account);
        transaction.setValor(withdrawAmount);
        transaction.setTipo(TransactionType.SAQUE);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.withdraw(accountId, withdrawAmount);

        assertEquals(initialBalance.subtract(withdrawAmount), account.getSaldo());
        assertEquals(TransactionType.SAQUE, result.getTipo());
        assertEquals(withdrawAmount, result.getValor());
        assertEquals(account, result.getConta());

        verify(transactionRepository).save(any(Transaction.class));

        verify(accountRepository).save(account);
    }

    @Test
    void testWithdrawExceedsDailyLimit() {
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("500");

        Account account = new Account();
        account.setId(accountId);
        account.setSaldo(new BigDecimal("1000"));
        account.setLimiteDiarioSaque(new BigDecimal("1000"));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Transaction previousWithdrawal = new Transaction();
        previousWithdrawal.setTipo(TransactionType.SAQUE);
        previousWithdrawal.setValor(new BigDecimal("600"));
        when(transactionRepository.findByContaAndDataHoraBetween(any(), any(), any()))
                .thenReturn(List.of(previousWithdrawal));

        assertThrows(WithdrawalLimitExceededException.class, () -> {
            transactionService.withdraw(accountId, amount);
        });

        verify(accountRepository, never()).save(any());
    }

    @Test
    void testDepositThrowsAccountBlockedException() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);

        Account blockedAccount = new Account();
        blockedAccount.setId(accountId);
        blockedAccount.setBloqueada(true);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(blockedAccount));

        AccountBlockedException exception = assertThrows(AccountBlockedException.class,
                () -> transactionService.deposit(accountId, amount));

        assertEquals("A conta está bloqueada e não pode receber depósitos.", exception.getMessage());
    }

    @Test
    void testWithdrawThrowsAccountBlockedException() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);

        Account blockedAccount = new Account();
        blockedAccount.setId(accountId);
        blockedAccount.setBloqueada(true);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(blockedAccount));

        AccountBlockedException exception = assertThrows(AccountBlockedException.class,
                () -> transactionService.withdraw(accountId, amount));

        assertEquals("A conta está bloqueada e não pode realizar saques.", exception.getMessage());
    }

    @Test
    void testWithdrawThrowsInsufficientBalanceException() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);

        Account account = new Account();
        account.setId(accountId);
        account.setSaldo(BigDecimal.valueOf(50));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class,
                () -> transactionService.withdraw(accountId, amount));

        assertEquals("Saldo insuficiente!", exception.getMessage());
    }

    @Test
    void testWithdrawThrowsWithdrawalLimitExceededException() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(3000);

        Account account = new Account();
        account.setId(accountId);
        account.setSaldo(BigDecimal.valueOf(5000));
        account.setLimiteDiarioSaque(BigDecimal.valueOf(1000));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        WithdrawalLimitExceededException exception = assertThrows(WithdrawalLimitExceededException.class,
                () -> transactionService.withdraw(accountId, amount));

        assertEquals("O valor total de saques do dia excede o limite diário permitido.", exception.getMessage());
    }

    @Test
    void testWithdrawExceedsDailyLimitWithMultipleTransactions() {
        Long accountId = 1L;
        Account account = new Account();
        account.setId(accountId);
        account.setSaldo(BigDecimal.valueOf(5000));
        account.setLimiteDiarioSaque(BigDecimal.valueOf(1000));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Transaction previousTransaction1 = new Transaction();
        previousTransaction1.setTipo(TransactionType.SAQUE);
        previousTransaction1.setValor(BigDecimal.valueOf(600));

        Transaction previousTransaction2 = new Transaction();
        previousTransaction2.setTipo(TransactionType.SAQUE);
        previousTransaction2.setValor(BigDecimal.valueOf(400));

        when(transactionRepository.findByContaAndDataHoraBetween(any(Account.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(previousTransaction1, previousTransaction2));

        BigDecimal newWithdrawalAmount = BigDecimal.valueOf(200);

        WithdrawalLimitExceededException exception = assertThrows(WithdrawalLimitExceededException.class, () -> {
            transactionService.withdraw(accountId, newWithdrawalAmount);
        });

        assertEquals("O valor total de saques do dia excede o limite diário permitido.", exception.getMessage());
    }



}
