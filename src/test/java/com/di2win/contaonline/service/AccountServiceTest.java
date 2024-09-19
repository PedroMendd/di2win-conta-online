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

public class AccountServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

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

        accountService.deposit(accountId, depositAmount);

        assertEquals(BigDecimal.valueOf(3000), account.getSaldo());
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

        accountService.withdraw(accountId, withdrawAmount);

        assertEquals(initialBalance.subtract(withdrawAmount), account.getSaldo());
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
            accountService.withdraw(accountId, amount);
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
                () -> accountService.deposit(accountId, amount));

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
                () -> accountService.withdraw(accountId, amount));

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
                () -> accountService.withdraw(accountId, amount));

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
                () -> accountService.withdraw(accountId, amount));

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
            accountService.withdraw(accountId, newWithdrawalAmount);
        });

        assertEquals("O valor total de saques do dia excede o limite diário permitido.", exception.getMessage());
    }

    @Test
    void testGetTransactionsByPeriod() {
        Long accountId = 1L;
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now();

        Account account = new Account();
        account.setId(accountId);

        Transaction transaction1 = new Transaction();
        transaction1.setConta(account);
        transaction1.setValor(BigDecimal.valueOf(100));
        transaction1.setTipo(TransactionType.DEPOSITO);
        transaction1.setDataHora(start.plusDays(1));

        Transaction transaction2 = new Transaction();
        transaction2.setConta(account);
        transaction2.setValor(BigDecimal.valueOf(200));
        transaction2.setTipo(TransactionType.SAQUE);
        transaction2.setDataHora(end.minusDays(1));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.findByContaAndDataHoraBetween(account, start, end))
                .thenReturn(List.of(transaction1, transaction2));

        List<Transaction> transactions = accountService.getTransactionsByPeriod(accountId, start, end);

        assertEquals(2, transactions.size());
        verify(transactionRepository, times(1)).findByContaAndDataHoraBetween(account, start, end);
    }

}
