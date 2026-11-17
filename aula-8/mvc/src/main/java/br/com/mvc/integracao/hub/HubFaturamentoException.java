package br.com.mvc.integracao.hub;

/** Falha ao chamar o hub de faturamento (fora do ar, timeout, erro HTTP, resposta inválida). */
public class HubFaturamentoException extends RuntimeException {

  public HubFaturamentoException(String mensagem) {
    super(mensagem);
  }

  public HubFaturamentoException(String mensagem, Throwable causa) {
    super(mensagem, causa);
  }
}
