package br.com.hub.domain.model;

import br.com.hub.domain.exception.RegraFaturamentoException;
import java.math.BigDecimal;

public record ItemPedido(String descricao, int quantidade, BigDecimal precoUnitario) {

  public ItemPedido {
    if (descricao == null || descricao.isBlank()) {
      throw new RegraFaturamentoException("Item sem descrição.");
    }
    if (quantidade <= 0) {
      throw new RegraFaturamentoException("Quantidade do item deve ser maior que zero.");
    }
    if (precoUnitario == null || precoUnitario.signum() < 0) {
      throw new RegraFaturamentoException("Preço unitário inválido.");
    }
  }

  public BigDecimal subtotal() {
    return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
  }
}
