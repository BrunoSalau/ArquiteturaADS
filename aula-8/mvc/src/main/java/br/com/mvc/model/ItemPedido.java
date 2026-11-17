package br.com.mvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class ItemPedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private Pedido pedido;

  @ManyToOne(optional = false)
  private Produto produto;

  @Column(nullable = false)
  private Integer quantidade;

  /** Preço copiado do produto no momento da venda. */
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal precoUnitario;

  public ItemPedido() {
  }

  public ItemPedido(Pedido pedido, Produto produto, Integer quantidade, BigDecimal precoUnitario) {
    this.pedido = pedido;
    this.produto = produto;
    this.quantidade = quantidade;
    this.precoUnitario = precoUnitario;
  }

  public BigDecimal getSubtotal() {
    return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
  }

  public Long getId() {
    return id;
  }

  public Pedido getPedido() {
    return pedido;
  }

  public Produto getProduto() {
    return produto;
  }

  public Integer getQuantidade() {
    return quantidade;
  }

  public void setQuantidade(Integer quantidade) {
    this.quantidade = quantidade;
  }

  public BigDecimal getPrecoUnitario() {
    return precoUnitario;
  }
}
