package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class VehicleNotFoundException extends BusinessException {

    public VehicleNotFoundException(UUID id) {
        super("Veículo não encontrado com o ID: " + id, HttpStatus.NOT_FOUND);
    }
}