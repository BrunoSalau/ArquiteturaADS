package br.com.hub.adapter.in.rest;

import br.com.hub.domain.model.ItemPedido;
import br.com.hub.domain.model.Pedido;
import java.math.BigDecimal;
import java.util.List;

/** JSON recebido no POST /faturas. */
public record PedidoRequest(Long pedidoId, String cliente, String emailCliente, BigDecimal valorTotal,
    List<ItemRequest> itens) {

  public record ItemRequest(String descricao, Integer quantidade, BigDecimal precoUnitario) {
  }

  public Pedido paraDominio() {
    List<ItemPedido> itensDominio = itens == null ? List.of() : itens.stream()
        .map(i -> new ItemPedido(i.descricao(), i.quantidade() == null ? 0 : i.quantidade(),
            i.precoUnitario()))
        .toList();
    return new Pedido(pedidoId, cliente, emailCliente, valorTotal, itensDominio);
  }
}
