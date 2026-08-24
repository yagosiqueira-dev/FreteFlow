package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

public class InactiveResourceException extends BusinessException {

    public InactiveResourceException(String resourceName) {
        super(resourceName + " está desativado(a) e não pode ser usado(a) em um novo frete", HttpStatus.CONFLICT);
    }
}