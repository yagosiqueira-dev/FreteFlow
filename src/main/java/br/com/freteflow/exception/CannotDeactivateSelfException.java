package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

public class CannotDeactivateSelfException extends BusinessException {

    public CannotDeactivateSelfException() {
        super("Você não pode desativar sua própria conta", HttpStatus.CONFLICT);
    }
}
