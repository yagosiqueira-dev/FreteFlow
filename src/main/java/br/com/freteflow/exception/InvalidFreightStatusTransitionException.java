package br.com.freteflow.exception;

import br.com.freteflow.entity.FreightStatus;
import org.springframework.http.HttpStatus;

public class InvalidFreightStatusTransitionException extends BusinessException {

  public InvalidFreightStatusTransitionException(FreightStatus from, FreightStatus to) {
    super(String.format("Não é possível mudar o status de %s para %s", from, to), HttpStatus.CONFLICT);
  }
}
