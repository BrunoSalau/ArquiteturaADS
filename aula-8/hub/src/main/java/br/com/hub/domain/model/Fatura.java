package br.com.hub.domain.model;

import br.com.hub.domain.exception.RegraFaturamentoException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade de domínio. Não depende de framework (nem JPA, nem Spring).
 * As restrições dos requisitos ficam aqui, no núcleo do hexágono.
 */
public class Fatura {

  private final UUID id;
  private final Long pedidoId;
  private final String cliente;
  private final String emailCliente;
  private final BigDecimal valor;
  private final LocalDateTime criadaEm;
  private StatusFatura status;
  private String codigoBarrasBoleto;
  private String chaveAcessoNotaFiscal;
  private String hashEmail;

  public Fatura(UUID id, Long pedidoId, String cliente, String emailCliente, BigDecimal valor,
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

  /** RF01: cria a fatura a partir do pedido recebido. */
  public static Fatura criar(Pedido pedido) {
    return new Fatura(UUID.randomUUID(), pedido.pedidoId(), pedido.cliente(),
        pedido.emailCliente(), pedido.valorTotal(), LocalDateTime.now(), StatusFatura.CRIADA,
        null, null, null);
  }

  /** RF02 – restrição: o boleto deve gerar um código de barras (44 dígitos), guardado na fatura. */
  public void registrarBoleto(String codigoBarras) {
    if (codigoBarras == null || !codigoBarras.matches("\\d{44}")) {
      throw new RegraFaturamentoException("O boleto emitido não gerou um código de barras válido.");
    }
    this.codigoBarrasBoleto = codigoBarras;
    this.status = StatusFatura.BOLETO_EMITIDO;
  }

  /** RF03 – restrição: a nota deve gerar uma chave de acesso de 44 dígitos, guardada na fatura. */
  public void registrarNotaFiscal(String chaveAcesso) {
    if (chaveAcesso == null || !chaveAcesso.matches("\\d{44}")) {
      throw new RegraFaturamentoException("A nota fiscal não gerou uma chave de acesso de 44 dígitos.");
    }
    this.chaveAcessoNotaFiscal = chaveAcesso;
    this.status = StatusFatura.NOTA_FISCAL_EMITIDA;
  }

  /** RF04 – restrição: o e-mail enviado deve gerar um hash do conteúdo (SHA-256), guardado na fatura. */
  public void registrarEnvioEmail(String hashConteudo) {
    if (hashConteudo == null || !hashConteudo.matches("[0-9a-f]{64}")) {
      throw new RegraFaturamentoException("O e-mail enviado não gerou um hash SHA-256 do conteúdo.");
    }
    this.hashEmail = hashConteudo;
    this.status = StatusFatura.FATURADA;
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
