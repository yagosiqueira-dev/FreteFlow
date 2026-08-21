package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class DriverNotFoundException extends BusinessException {

    public DriverNotFoundException(UUID id) {
        super("Motorista não encontrado com o ID: " + id, HttpStatus.NOT_FOUND);
    }
}