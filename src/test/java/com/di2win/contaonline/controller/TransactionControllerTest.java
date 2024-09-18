package com.di2win.contaonline.controller;

import com.di2win.contaonline.entity.Account;
import com.di2win.contaonline.entity.Client;
import com.di2win.contaonline.entity.Transaction;
import com.di2win.contaonline.repository.AccountRepository;
import com.di2win.contaonline.repository.ClientRepository;
import com.di2win.contaonline.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Account account;

    @BeforeEach
    public void setup() {

        Optional<Client> optionalClient = clientRepository.findByCpf("12345678900");
        Client client;

        if (optionalClient.isEmpty()) {
            client = new Client();
            client.setNome("Cliente Teste");
            client.setCpf("12345678900");
            client.setDataNascimento(LocalDate.of(1990, 1, 1));
            clientRepository.save(client);
        } else {
            client = optionalClient.get();
        }

        Optional<Account> optionalAccount = accountRepository.findByCliente(client);
        if (optionalAccount.isEmpty()) {
            account = new Account();
            account.setAgencia("1234");
            account.setNumeroConta("0001");
            account.setSaldo(BigDecimal.ZERO);
            account.setBloqueada(false);
            account.setLimiteDiarioSaque(BigDecimal.valueOf(1000));
            account.setCliente(client);
            accountRepository.save(account);
        } else {
            account = optionalAccount.get();
        }
    }

    @AfterEach
    public void cleanUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        clientRepository.deleteAll();
    }


    @Test
    public void testDepositSuccess() throws Exception {
        BigDecimal amount = new BigDecimal("500");

        mockMvc.perform(post("/api/transactions/{accountId}/deposit", account.getId())
                        .param("amount", amount.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(amount))
                .andExpect(jsonPath("$.conta").doesNotExist());

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(amount, updatedAccount.getSaldo());

        List<Transaction> transactions = updatedAccount.getTransactions();
        assertFalse(transactions.isEmpty(), "Transação não registrada");
        assertEquals(amount, transactions.get(0).getValor(), "Valor da transação incorreto");
    }


    @Test
    public void testWithdrawSuccess() throws Exception {
        account.setSaldo(BigDecimal.valueOf(500));
        accountRepository.save(account);

        BigDecimal amount = new BigDecimal("200");

        mockMvc.perform(post("/api/transactions/{accountId}/withdraw", account.getId())
                        .param("amount", amount.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(amount))
                .andExpect(jsonPath("$.conta").doesNotExist());

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(BigDecimal.valueOf(300), updatedAccount.getSaldo());
    }




    @Test
    public void testGetTransactionsByPeriod() throws Exception {
        String start = "2024-09-17T00:00:00";
        String end = "2024-09-18T23:59:59";

        mockMvc.perform(get("/api/transactions/{accountId}/period", account.getId())
                        .param("start", start)
                        .param("end", end)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
