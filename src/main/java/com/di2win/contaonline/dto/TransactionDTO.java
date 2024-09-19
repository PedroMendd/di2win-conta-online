package com.di2win.contaonline.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private BigDecimal valor;
    private String tipo;
    private LocalDateTime dataHora;
}
