package br.com.mvc.model;

public enum StatusPedido {
  /** Pedido em montagem: itens podem ser incluídos e removidos. */
  ABERTO("Aberto"),
  /** Pedido fechado (estoque baixado), mas o hub ainda não devolveu o ID da fatura. */
  FECHADO("Fechado - aguardando fatura"),
  /** Pedido fechado e faturado: o ID da fatura devolvido pelo hub está guardado. */
  FATURADO("Faturado");

  private final String descricao;

  StatusPedido(String descricao) {
    this.descricao = descricao;
  }

  public String getDescricao() {
    return descricao;
  }
}
