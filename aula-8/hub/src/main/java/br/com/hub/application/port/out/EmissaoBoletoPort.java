package br.com.hub.application.port.out;

import br.com.hub.domain.model.Fatura;

/** Porta de saída: emissão de boleto (o adaptador usa fila). Retorna o código de barras. */
public interface EmissaoBoletoPort {

  String emitir(Fatura fatura);
}
