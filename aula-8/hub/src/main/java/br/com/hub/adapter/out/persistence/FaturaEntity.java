package br.com.hub.adapter.out.persistence;

import br.com.hub.domain.model.StatusFatura;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Modelo de persistência (JPA). Fica no adaptador, separado da entidade de domínio. */
@Entity
@Table(name = "fatura")
public class FaturaEntity {

  @Id
  private UUID id;

  @Column(nullable = false)
  private Long pedidoId;

  @Column(nullable = false)
  private String cliente;

  @Column(nullable = false)
  private String emailCliente;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal valor;

  @Column(nullable = false)
  private LocalDateTime criadaEm;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatusFatura status;

  @Column(length = 44)
  private String codigoBarrasBoleto;

  @Column(length = 44)
  private String chaveAcessoNotaFiscal;

  @Column(length = 64)
  private String hashEmail;

  protected FaturaEntity() {
  }

  public FaturaEntity(UUID id, Long pedidoId, String cliente, String emailCliente, BigDecimal valor,
      LocalDateTime criadaEm, StatusFatura status, String codigoBarrasBoleto,
      String chaveAcessoNotaFiscal, String hashEmail) {
    this.id = id;
    this.pedidoId = pedidoId;
    this.cliente = cliente;
    this.emailCliente = emailCliente;
    this.valor = valor;
    this.criadaEm = criadaEm;
    this.status = status;
    this.codigoBarrasBoleto = codigoBarrasBoleto;
    this.chaveAcessoNotaFiscal = chaveAcessoNotaFiscal;
    this.hashEmail = hashEmail;
  }

  public UUID getId() {
    return id;
  }

  public Long getPedidoId() {
    return pedidoId;
  }

  public String getCliente() {
    return cliente;
  }

  public String getEmailCliente() {
    return emailCliente;
  }

  public BigDecimal getValor() {
    return valor;
  }

  public LocalDateTime getCriadaEm() {
    return criadaEm;
  }

  public StatusFatura getStatus() {
    return status;
  }

  public String getCodigoBarrasBoleto() {
    return codigoBarrasBoleto;
  }

  public String getChaveAcessoNotaFiscal() {
    return chaveAcessoNotaFiscal;
  }

  public String getHashEmail() {
    return hashEmail;
  }
}
