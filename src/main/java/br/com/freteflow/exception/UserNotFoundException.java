package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;
import java.util.UUID;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(UUID id) {
        super("Usuário não encontrado. ID: " + id, HttpStatus.NOT_FOUND);
    }
}