package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

public class TooManyLoginAttemptsException extends BusinessException {

    public TooManyLoginAttemptsException() {
        super("Muitas tentativas de login falhas. Tente novamente em alguns minutos.", HttpStatus.TOO_MANY_REQUESTS);
    }
}