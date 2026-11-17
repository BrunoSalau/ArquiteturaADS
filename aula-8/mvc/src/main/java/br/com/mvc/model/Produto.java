package br.com.mvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

@Entity
public class Produto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nome;

  private String descricao;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal preco = BigDecimal.ZERO;

  @Column(nullable = false)
  private Integer quantidadeEstoque = 0;

  /** Todo produto nasce INDISPONÍVEL (sem estoque). */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatusProduto status = StatusProduto.INDISPONIVEL;

  public Produto() {
  }

  public Produto(String nome, String descricao, BigDecimal preco) {
    this.nome = nome;
    this.descricao = descricao;
    this.preco = preco;
  }

  public boolean isDescontinuado() {
    return status == StatusProduto.DESCONTINUADO;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public BigDecimal getPreco() {
    return preco;
  }

  public void setPreco(BigDecimal preco) {
    this.preco = preco;
  }

  public Integer getQuantidadeEstoque() {
    return quantidadeEstoque;
  }

  public void setQuantidadeEstoque(Integer quantidadeEstoque) {
    this.quantidadeEstoque = quantidadeEstoque;
  }

  public StatusProduto getStatus() {
    return status;
  }

  public void setStatus(StatusProduto status) {
    this.status = status;
  }
}
