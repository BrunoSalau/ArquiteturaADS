package br.com.mvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class MovimentacaoEstoque {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private Produto produto;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TipoMovimentacao tipo;

  @Column(nullable = false)
  private Integer quantidade;

  /** Saldo do produto logo após a movimentação. */
  @Column(nullable = false)
  private Integer saldoApos;

  @Column(nullable = false)
  private LocalDateTime dataHora;

  private String observacao;

  public MovimentacaoEstoque() {
  }

  public MovimentacaoEstoque(Produto produto, TipoMovimentacao tipo, Integer quantidade,
      Integer saldoApos, String observacao) {
    this.produto = produto;
    this.tipo = tipo;
    this.quantidade = quantidade;
    this.saldoApos = saldoApos;
    this.observacao = observacao;
    this.dataHora = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public Produto getProduto() {
    return produto;
  }

  public TipoMovimentacao getTipo() {
    return tipo;
  }

  public Integer getQuantidade() {
    return quantidade;
  }

  public Integer getSaldoApos() {
    return saldoApos;
  }

  public LocalDateTime getDataHora() {
    return dataHora;
  }

  public String getObservacao() {
    return observacao;
  }
}
