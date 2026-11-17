package br.com.hub.application.port.out;

import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.Pedido;

/** Porta de saída: notificação do cliente (o adaptador usa SMTP). Retorna o hash do conteúdo. */
public interface NotificacaoClientePort {

  String notificar(Fatura fatura, Pedido pedido);
}
