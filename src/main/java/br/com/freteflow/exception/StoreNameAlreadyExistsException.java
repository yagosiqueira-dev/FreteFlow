package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

public class StoreNameAlreadyExistsException extends BusinessException {

    public StoreNameAlreadyExistsException(String name) {
        super("Já existe uma loja/rota cadastrada com o nome: " + name, HttpStatus.CONFLICT);
    }
}
