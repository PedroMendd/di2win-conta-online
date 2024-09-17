package com.di2win.contaonline.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table (name = "contas")
@Data
public class Account{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numeroConta;

    @Column(nullable = false)
    private String agencia;

    @Column(nullable = false)
    private BigDecimal saldo;

    @Column(nullable = false)
    private boolean bloqueada;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Client cliente;

}
