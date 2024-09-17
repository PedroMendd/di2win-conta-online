package com.di2win.contaonline.exception.account;

public class WithdrawalLimitExceededException extends RuntimeException {
    public WithdrawalLimitExceededException(String message) {
        super(message);
    }
}