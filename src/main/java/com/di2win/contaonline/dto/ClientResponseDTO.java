package com.di2win.contaonline.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ClientResponseDTO {

    private Long id;
    private String cpf;
    private String nome;
    private LocalDate dataNascimento;
}
