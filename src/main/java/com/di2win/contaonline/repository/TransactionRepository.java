package com.di2win.contaonline.repository;

import com.di2win.contaonline.entity.Account;
import com.di2win.contaonline.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByContaId (Long contaId);

    List<Transaction> findByContaAndDataHoraBetween(Account conta, LocalDateTime start, LocalDateTime end);

}
