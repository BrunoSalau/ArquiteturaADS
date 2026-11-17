package br.com.hub.application.port.out;

import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.Pedido;

/** Porta de saída: emissão de nota fiscal (o adaptador usa SOAP). Retorna a chave de acesso. */
public interface EmissaoNotaFiscalPort {

  String emitir(Fatura fatura, Pedido pedido);
}
