package com.di2win.contaonline.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AccountCreationDTO {

    @NotNull(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos")
    private String cpf;

}
