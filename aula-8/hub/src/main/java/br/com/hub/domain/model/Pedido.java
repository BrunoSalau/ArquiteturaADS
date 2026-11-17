package br.com.hub.domain.model;

import br.com.hub.domain.exception.RegraFaturamentoException;
import java.math.BigDecimal;
import java.util.List;

/** Pedido recebido da aplicação de vendas para ser faturado. */
public record Pedido(Long pedidoId, String cliente, String emailCliente, BigDecimal valorTotal,
    List<ItemPedido> itens) {

  public Pedido {
    if (pedidoId == null) {
      throw new RegraFaturamentoException("Pedido sem identificador.");
    }
    if (cliente == null || cliente.isBlank()) {
      throw new RegraFaturamentoException("Pedido sem cliente.");
    }
    if (emailCliente == null || !emailCliente.contains("@")) {
      throw new RegraFaturamentoException("E-mail do cliente inválido.");
    }
    if (valorTotal == null || valorTotal.signum() <= 0) {
      throw new RegraFaturamentoException("Valor total do pedido deve ser maior que zero.");
    }
    itens = itens == null ? List.of() : List.copyOf(itens);
  }
}
