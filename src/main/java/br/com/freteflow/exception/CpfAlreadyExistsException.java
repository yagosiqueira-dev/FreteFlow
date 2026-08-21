package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

public class CpfAlreadyExistsException extends BusinessException {

    public CpfAlreadyExistsException(String cpf) {
        super("Já existe um motorista cadastrado com o CPF: " + cpf, HttpStatus.CONFLICT);
    }
}
