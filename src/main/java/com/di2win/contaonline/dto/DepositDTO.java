package com.di2win.contaonline.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositDTO {

    @NotNull(message = "O valor do depósito é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor do depósito deve ser maior que zero.")
    private BigDecimal amount;
}
