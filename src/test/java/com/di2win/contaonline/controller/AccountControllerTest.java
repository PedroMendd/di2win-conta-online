package com.di2win.contaonline.controller;

import com.di2win.contaonline.entity.Account;
import com.di2win.contaonline.entity.Client;
import com.di2win.contaonline.repository.AccountRepository;
import com.di2win.contaonline.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Client client;

    @BeforeEach
    public void setup() {
        accountRepository.deleteAll();
        clientRepository.deleteAll();

        client = new Client();
        client.setNome("Cliente Teste");
        client.setCpf("12345678900");
        client.setDataNascimento(LocalDate.of(1990, 1, 1));
        clientRepository.save(client);
    }

    @Test
    public void testCreateAccountSuccess() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .param("cpf", "12345678900")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.agencia").value("1234"))
                .andExpect(jsonPath("$.numeroConta").isString())
                .andExpect(jsonPath("$.saldo").value(0))
                .andExpect(jsonPath("$.bloqueada").value(false))
                .andExpect(jsonPath("$.limiteDiarioSaque").value(1000));
    }

    @Test
    public void testCreateAccountClientNotFound() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .param("cpf", "00000000000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cliente não encontrado com CPF: 00000000000"));
    }

    @Test
    public void testGetBalanceSuccess() throws Exception {
        Account account = new Account();
        account.setAgencia("1234");
        account.setNumeroConta("00000001");
        account.setSaldo(BigDecimal.valueOf(500));
        account.setBloqueada(false);
        account.setLimiteDiarioSaque(BigDecimal.valueOf(1000));
        account.setCliente(client);
        accountRepository.save(account);

        mockMvc.perform(get("/api/accounts/{accountId}/balance", account.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }

    @Test
    public void testGetBalanceAccountNotFound() throws Exception {
        mockMvc.perform(get("/api/accounts/{accountId}/balance", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Conta não encontrada: 999"));
    }

    @Test
    public void testDepositSuccess() throws Exception {
        Account account = new Account();
        account.setAgencia("1234");
        account.setNumeroConta("00000001");
        account.setSaldo(BigDecimal.ZERO);
        account.setBloqueada(false);
        account.setLimiteDiarioSaque(BigDecimal.valueOf(1000));
        account.setCliente(client);
        accountRepository.save(account);

        mockMvc.perform(post("/api/accounts/{accountId}/deposit", account.getId())
                        .param("amount", "500")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(500));
    }

    @Test
    public void testDepositAccountNotFound() throws Exception {
        mockMvc.perform(post("/api/accounts/{accountId}/deposit", 999L) // ID inexistente
                        .param("amount", "500")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Conta não encontrada: 999"));
    }

    @Test
    public void testWithdrawSuccess() throws Exception {
        // Criar uma conta
        Account account = new Account();
        account.setAgencia("1234");
        account.setNumeroConta("00000001");
        account.setSaldo(BigDecimal.valueOf(500));
        account.setBloqueada(false);
        account.setLimiteDiarioSaque(BigDecimal.valueOf(1000));
        account.setCliente(client);
        accountRepository.save(account);

        mockMvc.perform(post("/api/accounts/{accountId}/withdraw", account.getId())
                        .param("amount", "200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(300));
    }

    @Test
    public void testWithdrawInsufficientBalance() throws Exception {
        Account account = new Account();
        account.setAgencia("1234");
        account.setNumeroConta("00000001");
        account.setSaldo(BigDecimal.valueOf(100));
        account.setBloqueada(false);
        account.setLimiteDiarioSaque(BigDecimal.valueOf(1000));
        account.setCliente(client);
        accountRepository.save(account);

        mockMvc.perform(post("/api/accounts/{accountId}/withdraw", account.getId())
                        .param("amount", "200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Saldo insuficiente!"));
    }

    @Test
    public void testWithdrawAccountBlocked() throws Exception {
        Account account = new Account();
        account.setAgencia("1234");
        account.setNumeroConta("00000001");
        account.setSaldo(BigDecimal.valueOf(500));
        account.setBloqueada(true);
        account.setLimiteDiarioSaque(BigDecimal.valueOf(1000));
        account.setCliente(client);
        accountRepository.save(account);

        mockMvc.perform(post("/api/accounts/{accountId}/withdraw", account.getId())
                        .param("amount", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("A conta está bloqueada e não pode realizar saques."));
    }

    @Test
    public void testWithdrawWithdrawalLimitExceeded() throws Exception {
        Account account = new Account();
        account.setAgencia("1234");
        account.setNumeroConta("00000001");
        account.setSaldo(BigDecimal.valueOf(500));
        account.setBloqueada(false);
        account.setLimiteDiarioSaque(BigDecimal.valueOf(100));
        account.setCliente(client);
        accountRepository.save(account);

        mockMvc.perform(post("/api/accounts/{accountId}/withdraw", account.getId())
                        .param("amount", "200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("O valor total de saques do dia excede o limite diário permitido."));
    }

    @Test
    public void testBlockAccountSuccess() throws Exception {
        Account account = new Account();
        account.setAgencia("1234");
        account.setNumeroConta("00000001");
        account.setSaldo(BigDecimal.valueOf(500));
        account.setBloqueada(false);
        account.setLimiteDiarioSaque(BigDecimal.valueOf(1000));
        account.setCliente(client);
        accountRepository.save(account);

        mockMvc.perform(post("/api/accounts/{accountId}/block", account.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Account blockedAccount = accountRepository.findById(account.getId()).orElse(null);
        assert blockedAccount != null;
        assert blockedAccount.isBloqueada();
    }

    @Test
    public void testBlockAccountNotFound() throws Exception {
        mockMvc.perform(post("/api/accounts/{accountId}/block", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Conta não encontrada: 999"));
    }

    @Test
    public void testDeleteAccountSuccess() throws Exception {
        Account account = new Account();
        account.setAgencia("1234");
        account.setNumeroConta("00000001");
        account.setSaldo(BigDecimal.valueOf(500));
        account.setBloqueada(false);
        account.setLimiteDiarioSaque(BigDecimal.valueOf(1000));
        account.setCliente(client);
        accountRepository.save(account);

        mockMvc.perform(delete("/api/accounts/{accountId}", account.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        boolean exists = accountRepository.existsById(account.getId());
        assert !exists;
    }

    @Test
    public void testDeleteAccountNotFound() throws Exception {
        mockMvc.perform(delete("/api/accounts/{accountId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Conta não encontrada: 999"));
    }
}
