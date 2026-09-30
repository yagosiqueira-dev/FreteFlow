package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

public class CannotDemoteSelfException extends BusinessException {

    public CannotDemoteSelfException() {
        super("Você não pode remover seu próprio privilégio de administrador", HttpStatus.CONFLICT);
    }
}