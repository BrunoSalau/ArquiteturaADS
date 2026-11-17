package br.com.mvc.service;

/** Erro de regra de negócio, exibido como mensagem na tela. */
public class RegraNegocioException extends RuntimeException {

  public RegraNegocioException(String mensagem) {
    super(mensagem);
  }
}
