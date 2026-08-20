package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

public class LicensePlateAlreadyExistsException extends BusinessException {

    public LicensePlateAlreadyExistsException(String licensePlate) {
        super("Já existe um veículo cadastrado com a placa: " + licensePlate, HttpStatus.CONFLICT);
    }
}