package com.di2win.contaonline.util;

import com.di2win.contaonline.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AccountNumberGenerator {

    public String generateUniqueAccountNumber(AccountRepository accountRepository) {
        Random random = new Random();
        String numeroConta;

        do {
            numeroConta = String.format("%08d", random.nextInt(100000000));
        } while (accountRepository.findByNumeroConta(numeroConta).isPresent());

        return numeroConta;
    }
}
