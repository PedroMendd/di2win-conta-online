package com.di2win.contaonline.service;

import com.di2win.contaonline.entity.Account;
import com.di2win.contaonline.entity.Client;
import com.di2win.contaonline.exception.account.AccountBlockedException;
import com.di2win.contaonline.exception.account.AccountNotFoundException;
import com.di2win.contaonline.exception.account.InsufficientBalanceException;
import com.di2win.contaonline.exception.account.WithdrawalLimitExceededException;
import com.di2win.contaonline.exception.client.ClientNotFoundException;
import com.di2win.contaonline.repository.AccountRepository;
import com.di2win.contaonline.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAccountSuccess() {
        Client client = new Client();
        client.setCpf("12345678900");
        when(clientRepository.findByCpf("12345678900")).thenReturn(Optional.of(client));

        when(accountRepository.findByNumeroConta(anyString())).thenReturn(Optional.empty());

        Account account = new Account();
        account.setAgencia("1234");
        account.setNumeroConta("00000001");
        account.setSaldo(BigDecimal.ZERO);
        account.setBloqueada(false);
        account.setCliente(client);

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account createdAccount = accountService.createAccount("12345678900");

        assertNotNull(createdAccount);
        assertEquals(BigDecimal.ZERO, createdAccount.getSaldo());
        assertFalse(createdAccount.isBloqueada());
        assertEquals("1234", createdAccount.getAgencia());

        verify(accountRepository, times(1)).save(any(Account.class));
    }


    @Test
    public void testCreateAccountClientNotFound() {
        when(clientRepository.findByCpf("12345678900")).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> accountService.createAccount("12345678900"));
    }

    @Test
    public void testFindByIdSuccess() {
        Account account = new Account();
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Account foundAccount = accountService.findById(1L);
        assertNotNull(foundAccount);
        assertEquals(1L, foundAccount.getId());
    }

    @Test
    public void testFindByIdNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.findById(1L));
    }

    @Test
    public void testDepositSuccess() {
        Account account = new Account();
        account.setId(1L);
        account.setSaldo(BigDecimal.ZERO);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        accountService.deposit(1L, BigDecimal.valueOf(500));

        verify(transactionService, times(1)).deposit(1L, BigDecimal.valueOf(500));
        assertEquals(BigDecimal.ZERO, account.getSaldo());
    }

    @Test
    public void testWithdrawSuccess() {
        Account account = new Account();
        account.setId(1L);
        account.setSaldo(BigDecimal.valueOf(500));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        accountService.withdraw(1L, BigDecimal.valueOf(200));

        verify(transactionService, times(1)).withdraw(1L, BigDecimal.valueOf(200));
        assertEquals(BigDecimal.valueOf(500), account.getSaldo());
    }

    @Test
    public void testDeleteAccountSuccess() {
        Account account = new Account();
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        accountService.deleteAccount(1L);

        verify(accountRepository, times(1)).delete(account);
    }

    @Test
    public void testCreateAccountThrowsClientNotFoundException() {
        when(clientRepository.findByCpf("12345678900")).thenReturn(Optional.empty());

        Exception exception = assertThrows(ClientNotFoundException.class, () -> {
            accountService.createAccount("12345678900");
        });

        assertEquals("Cliente não encontrado com CPF: 12345678900", exception.getMessage());
    }

    @Test
    public void testFindByIdThrowsAccountNotFoundException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(AccountNotFoundException.class, () -> {
            accountService.findById(1L);
        });

        assertEquals("Conta não encontrada: 1", exception.getMessage());
    }

    @Test
    public void testWithdrawThrowsInsufficientBalanceException() {
        Account account = new Account();
        account.setId(1L);
        account.setSaldo(BigDecimal.valueOf(100));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        doThrow(new InsufficientBalanceException("Saldo insuficiente na conta")).when(transactionService).withdraw(1L, BigDecimal.valueOf(200));

        Exception exception = assertThrows(InsufficientBalanceException.class, () -> {
            accountService.withdraw(1L, BigDecimal.valueOf(200));
        });

        assertEquals("Saldo insuficiente na conta", exception.getMessage());
    }

    @Test
    public void testWithdrawThrowsAccountBlockedException() {
        Account account = new Account();
        account.setId(1L);
        account.setBloqueada(true);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        doThrow(new AccountBlockedException("A conta está bloqueada")).when(transactionService).withdraw(1L, BigDecimal.valueOf(100));

        Exception exception = assertThrows(AccountBlockedException.class, () -> {
            accountService.withdraw(1L, BigDecimal.valueOf(100));
        });

        assertEquals("A conta está bloqueada e não pode realizar saques.", exception.getMessage());
    }

    @Test
    public void testWithdrawThrowsWithdrawalLimitExceededException() {
        Account account = new Account();
        account.setId(1L);
        account.setSaldo(BigDecimal.valueOf(500));
        account.setLimiteDiarioSaque(BigDecimal.valueOf(100));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        doThrow(new WithdrawalLimitExceededException("Limite diário de saque excedido")).when(transactionService).withdraw(1L, BigDecimal.valueOf(200));

        Exception exception = assertThrows(WithdrawalLimitExceededException.class, () -> {
            accountService.withdraw(1L, BigDecimal.valueOf(200));
        });

        assertEquals("Limite diário de saque excedido", exception.getMessage());
    }


}
