package br.com.freteflow.exception;

import org.springframework.http.HttpStatus;

public class MixedStoreOriginException extends BusinessException {

  public MixedStoreOriginException() {
    super("Todas as lojas do frete devem ter a mesma origem (ponto de carregamento)", HttpStatus.CONFLICT);
  }
}