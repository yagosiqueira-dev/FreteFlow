package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class StoreNotFoundException extends BusinessException {

    public StoreNotFoundException(UUID id) {
        super("Loja não encontrada com o ID: " + id, HttpStatus.NOT_FOUND);
    }
}