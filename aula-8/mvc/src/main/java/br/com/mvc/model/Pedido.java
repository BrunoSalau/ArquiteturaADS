package br.com.mvc.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Pedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String cliente;

  @Column(nullable = false)
  private String emailCliente;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatusPedido status = StatusPedido.ABERTO;

  @Column(nullable = false)
  private LocalDateTime criadoEm = LocalDateTime.now();

  private LocalDateTime fechadoEm;

  /** ID da fatura devolvido pelo hub de faturamento. */
  private String faturaId;

  /** Última falha na integração com o hub (null quando deu certo). */
  @Column(length = 1000)
  private String erroIntegracao;

  @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ItemPedido> itens = new ArrayList<>();

  public Pedido() {
  }

  public Pedido(String cliente, String emailCliente) {
    this.cliente = cliente;
    this.emailCliente = emailCliente;
  }

  public BigDecimal getValorTotal() {
    return itens.stream().map(ItemPedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public void adicionarItem(Produto produto, int quantidade) {
    for (ItemPedido item : itens) {
      if (item.getProduto().getId().equals(produto.getId())) {
        item.setQuantidade(item.getQuantidade() + quantidade);
        return;
      }
    }
    itens.add(new ItemPedido(this, produto, quantidade, produto.getPreco()));
  }

  public void removerItem(Long itemId) {
    itens.removeIf(item -> item.getId().equals(itemId));
  }

  public void fechar() {
    this.status = StatusPedido.FECHADO;
    this.fechadoEm = LocalDateTime.now();
  }

  public void registrarFatura(String faturaId) {
    this.faturaId = faturaId;
    this.erroIntegracao = null;
    this.status = StatusPedido.FATURADO;
  }

  public void registrarErroIntegracao(String erro) {
    this.erroIntegracao = erro == null ? null : erro.substring(0, Math.min(erro.length(), 1000));
  }

  public boolean isAberto() {
    return status == StatusPedido.ABERTO;
  }

  public boolean isAguardandoFatura() {
    return status == StatusPedido.FECHADO && faturaId == null;
  }

  public Long getId() {
    return id;
  }

  public String getCliente() {
    return cliente;
  }

  public String getEmailCliente() {
    return emailCliente;
  }

  public StatusPedido getStatus() {
    return status;
  }

  public LocalDateTime getCriadoEm() {
    return criadoEm;
  }

  public LocalDateTime getFechadoEm() {
    return fechadoEm;
  }

  public String getFaturaId() {
    return faturaId;
  }

  public String getErroIntegracao() {
    return erroIntegracao;
  }

  public List<ItemPedido> getItens() {
    return itens;
  }
}
