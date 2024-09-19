package com.di2win.contaonline.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountResponseDTO {

    private Long id;
    private String numeroConta;
    private String agencia;
    private BigDecimal saldo;
    private BigDecimal limiteDiarioSaque;
    private boolean bloqueada;
    private ClientResponseDTO cliente;
}
