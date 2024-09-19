package com.di2win.contaonline.service;

import com.di2win.contaonline.dto.AccountCreationDTO;
import com.di2win.contaonline.dto.TransactionDTO;
import com.di2win.contaonline.entity.Account;
import com.di2win.contaonline.entity.Client;
import com.di2win.contaonline.entity.Transaction;
import com.di2win.contaonline.entity.TransactionType;
import com.di2win.contaonline.exception.account.*;
import com.di2win.contaonline.exception.client.ClientNotFoundException;
import com.di2win.contaonline.repository.AccountRepository;
import com.di2win.contaonline.repository.ClientRepository;
import com.di2win.contaonline.repository.TransactionRepository;
import com.di2win.contaonline.util.AccountNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private static final String AGENCIA_PADRAO = "1234";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountNumberGenerator accountNumberGenerator;

    public Account createAccount(AccountCreationDTO accountCreationDTO) {
        Optional<Client> client = clientRepository.findByCpf(accountCreationDTO.getCpf());
        if (client.isEmpty()) {
            throw new ClientNotFoundException("Cliente não encontrado com CPF: " + accountCreationDTO.getCpf());
        }

        String numeroConta = accountNumberGenerator.generateUniqueAccountNumber(accountRepository);

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

        Transaction transaction = new Transaction();
        transaction.setConta(account);
        transaction.setValor(amount);
        transaction.setTipo(TransactionType.DEPOSITO);

        account.getTransactions().add(transaction);

        account.setSaldo(account.getSaldo().add(amount));

        transactionRepository.save(transaction);
        accountRepository.save(account);

        return findById(accountId);
    }


    public Account withdraw(Long accountId, BigDecimal amount) {
        Account account = findById(accountId);

        if (account.isBloqueada()) {
            throw new AccountBlockedException("A conta está bloqueada e não pode realizar saques.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do saque deve ser maior que zero.");
        }

        if (account.getSaldo().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente!");
        }

        BigDecimal totalSaquesDia = calcularTotalSaquesDia(account);
        if (totalSaquesDia.add(amount).compareTo(account.getLimiteDiarioSaque()) > 0) {
            throw new WithdrawalLimitExceededException("O valor total de saques do dia excede o limite diário permitido.");
        }

        Transaction transaction = new Transaction();
        transaction.setConta(account);
        transaction.setValor(amount);
        transaction.setTipo(TransactionType.SAQUE);

        account.getTransactions().add(transaction);
        account.setSaldo(account.getSaldo().subtract(amount));

        transactionRepository.save(transaction);
        accountRepository.save(account);

        return findById(accountId);
    }


    private BigDecimal calcularTotalSaquesDia(Account account) {
        LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);

        List<Transaction> saquesDoDia = transactionRepository.findByContaAndDataHoraBetween(account, inicioDoDia, fimDoDia);

        return saquesDoDia.stream()
                .filter(transacao -> transacao.getTipo() == TransactionType.SAQUE)
                .map(Transaction::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<TransactionDTO> getTransactionsByPeriod(Long accountId, LocalDateTime start, LocalDateTime end) {
        Account account = findById(accountId);
        List<Transaction> transactions = transactionRepository.findByContaAndDataHoraBetween(account, start, end);

        return transactions.stream()
                .map(transaction -> {
                    TransactionDTO dto = new TransactionDTO();
                    dto.setId(transaction.getId());
                    dto.setValor(transaction.getValor());
                    dto.setTipo(transaction.getTipo().toString());
                    dto.setDataHora(transaction.getDataHora());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public void blockAccount(Long accountId) {
        Account account = findById(accountId);

        if (account.isBloqueada()) {
            throw new IllegalStateException("A conta já está bloqueada.");
        }

        account.setBloqueada(true);
        accountRepository.save(account);
    }

    public void unblockAccount(Long accountId) {
        Account account = findById(accountId);

        if (!account.isBloqueada()) {
            throw new IllegalStateException("A conta já está desbloqueada.");
        }

        account.setBloqueada(false);
        accountRepository.save(account);
    }


    public void deleteAccount(Long accountId) {
        Account account = findById(accountId);

        if (account.isBloqueada()) {
            throw new AccountBlockedException("Conta bloqueada não pode ser deletada.");
        }

        if (!account.getTransactions().isEmpty()) {
            throw new IllegalStateException("Conta com transações não pode ser deletada.");
        }

        accountRepository.delete(account);
    }

}
