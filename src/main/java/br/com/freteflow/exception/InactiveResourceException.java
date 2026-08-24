package br.com.freteflow.exception;

public class InactiveResourceException extends RuntimeException {
  public InactiveResourceException(String message) {
    super(message);
  }
}
