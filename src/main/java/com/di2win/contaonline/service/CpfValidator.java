package com.di2win.contaonline.service;

import com.di2win.contaonline.exception.cpf.InvalidCpfFormatException;

public class CpfValidator {

    private CpfValidator() {}

    public static void validate(String cpf) {
        if (cpf == null || cpf.isEmpty()) {
            throw new InvalidCpfFormatException("O CPF não pode ser nulo ou vazio.");
        }

        if (cpf.length() != 11) {
            throw new InvalidCpfFormatException("O CPF deve conter 11 dígitos.");
        }

        if (cpf.chars().distinct().count() == 1) {
            throw new InvalidCpfFormatException("O CPF não pode conter todos os dígitos iguais.");
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int firstVerifier = 11 - (sum % 11);
        if (firstVerifier >= 10) {
            firstVerifier = 0;
        }

        if (firstVerifier != Character.getNumericValue(cpf.charAt(9))) {
            throw new InvalidCpfFormatException("O primeiro dígito verificador do CPF é inválido.");
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int secondVerifier = 11 - (sum % 11);
        if (secondVerifier >= 10) {
            secondVerifier = 0;
        }

        if (secondVerifier != Character.getNumericValue(cpf.charAt(10))) {
            throw new InvalidCpfFormatException("O segundo dígito verificador do CPF é inválido.");
        }
    }
}
