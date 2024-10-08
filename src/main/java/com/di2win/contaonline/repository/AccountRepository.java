package com.di2win.contaonline.repository;

import com.di2win.contaonline.entity.Account;
import com.di2win.contaonline.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByNumeroConta(String numeroConta);

    Optional<Account> findByCliente(Client cliente);
}
