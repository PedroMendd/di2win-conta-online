package com.di2win.contaonline.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "clientes")
@Data
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "CPF não pode estar em branco")
    private String cpf;

    @Column(nullable = false)
    @NotBlank(message = "Nome não pode estar em branco")
    private String nome;

    @Column(nullable = false)
    private LocalDate dataNascimento;
}
