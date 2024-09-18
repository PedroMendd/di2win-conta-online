package com.di2win.contaonline.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType tipo;

    @ManyToOne
    @JoinColumn(name = "conta_id", nullable = false)
    @JsonBackReference
    private Account conta;

    @PrePersist
    protected void onCreate() {
        this.dataHora = LocalDateTime.now();
    }
}
