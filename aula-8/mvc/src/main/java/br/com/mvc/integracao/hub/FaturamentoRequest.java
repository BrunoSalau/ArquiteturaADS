package br.com.mvc.integracao.hub;

import br.com.mvc.model.Pedido;
import java.math.BigDecimal;
import java.util.List;

/** JSON enviado ao hub no POST /faturas (mesmo contrato do PedidoRequest do hub). */
public record FaturamentoRequest(Long pedidoId, String cliente, String emailCliente,
    BigDecimal valorTotal, List<Item> itens) {

  public record Item(String descricao, Integer quantidade, BigDecimal precoUnitario) {
  }

  public static FaturamentoRequest de(Pedido pedido) {
    List<Item> itens = pedido.getItens().stream()
        .map(i -> new Item(i.getProduto().getNome(), i.getQuantidade(), i.getPrecoUnitario()))
        .toList();
    return new FaturamentoRequest(pedido.getId(), pedido.getCliente(), pedido.getEmailCliente(),
        pedido.getValorTotal(), itens);
  }
}
