package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class FreightNotFoundException extends BusinessException {

    public FreightNotFoundException(UUID id) {
        super("Frete não encontrado com o ID: " + id, HttpStatus.NOT_FOUND);
    }
}