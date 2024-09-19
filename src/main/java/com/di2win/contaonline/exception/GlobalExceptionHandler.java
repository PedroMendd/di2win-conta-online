package com.di2win.contaonline.exception;

import com.di2win.contaonline.exception.account.AccountBlockedException;
import com.di2win.contaonline.exception.account.AccountNotFoundException;
import com.di2win.contaonline.exception.account.InsufficientBalanceException;
import com.di2win.contaonline.exception.account.WithdrawalLimitExceededException;
import com.di2win.contaonline.exception.client.ClientNotFoundException;
import com.di2win.contaonline.exception.client.InvalidBirthDateException;
import com.di2win.contaonline.exception.client.InvalidNameException;
import com.di2win.contaonline.exception.cpf.CpfAlreadyExistsException;
import com.di2win.contaonline.exception.cpf.CpfInvalidException;
import com.di2win.contaonline.exception.cpf.InvalidCpfFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CpfAlreadyExistsException.class)
    public ResponseEntity<String> handleCpfAlreadyExistsException(CpfAlreadyExistsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCpfFormatException.class)
    public ResponseEntity<String> handleInvalidCpfFormatException(InvalidCpfFormatException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CpfInvalidException.class)
    public ResponseEntity<String> handleCpfInvalidException(CpfInvalidException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<String> handleClientNotFoundException(ClientNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidNameException.class)
    public ResponseEntity<String> handleInvalidName(InvalidNameException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidBirthDateException.class)
    public ResponseEntity<String> handleInvalidBirthDate(InvalidBirthDateException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<String> handleAccountNotFoundException(AccountNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccountBlockedException.class)
    public ResponseEntity<String> handleAccountBlockedException(AccountBlockedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(WithdrawalLimitExceededException.class)
    public ResponseEntity<String> handleWithdrawalLimitExceededException(WithdrawalLimitExceededException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return new ResponseEntity<>("Um erro inesperado ocorreu", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
