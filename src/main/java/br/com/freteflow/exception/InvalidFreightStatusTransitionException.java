package br.com.freteflow.exception;

public class InvalidFreightStatusTransitionException extends RuntimeException {
  public InvalidFreightStatusTransitionException(String message) {
    super(message);
  }
}
