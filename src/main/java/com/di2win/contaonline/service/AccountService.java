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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountService {

    private static final String AGENCIA_PADRAO = "1234";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    public Account createAccount(String cpf){
     Optional<Client> client = clientRepository.findByCpf(cpf);
     if (client.isEmpty()) {
         throw new ClientNotFoundException("Cliente não encontrado com CPF: " + cpf);
     }

     String numeroConta = generateUniqueAccountNumber();

     Account account = new Account();
     account.setCliente(client.get());
     account.setAgencia(AGENCIA_PADRAO);
     account.setNumeroConta(numeroConta);
     account.setSaldo(BigDecimal.ZERO);
     account.setBloqueada(false);
     account.setLimiteDiarioSaque(BigDecimal.valueOf(1000));

     return accountRepository.save(account);

    }

    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada: " + id));
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public Account deposit(Long accountId, BigDecimal amount) {
        Account account = findById(accountId);

        if (account.isBloqueada()) {
            throw new AccountBlockedException("A conta está bloqueada e não pode receber depósitos.");
        }

        account.setSaldo(account.getSaldo().add(amount));
        return accountRepository.save(account);
    }

    public Account withdraw(Long accountId, BigDecimal amount) {
        Account account = findById(accountId);

        if (account.isBloqueada()) {
            throw new AccountBlockedException("A conta está bloqueada e não pode realizar saques.");
        }

        if (account.getSaldo().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente!");
        }

        if (amount.compareTo(account.getLimiteDiarioSaque()) > 0) {
            throw new WithdrawalLimitExceededException("O valor do saque excede o limite diário permitido.");
        }

        account.setSaldo(account.getSaldo().subtract(amount));
        return accountRepository.save(account);
    }

    public void deleteAccount(Long accountId) {
        Account account = findById(accountId);
        accountRepository.delete(account);
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String numeroConta;
        boolean exists;

        do {
            numeroConta = String.format("%08d", random.nextInt(100000000));
            exists = accountRepository.findByNumeroConta(numeroConta).isPresent();
        } while (exists);

        return numeroConta;
    };

}
