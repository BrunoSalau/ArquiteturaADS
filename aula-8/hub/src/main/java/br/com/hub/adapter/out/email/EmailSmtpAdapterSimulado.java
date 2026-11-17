package br.com.hub.adapter.out.email;

import br.com.hub.application.port.out.NotificacaoClientePort;
import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.Pedido;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptador SIMULADO de notificação por e-mail via SMTP.
 * Monta a mensagem, registra no log o diálogo SMTP que seria feito e
 * devolve o hash SHA-256 do conteúdo enviado.
 */
@Component
public class EmailSmtpAdapterSimulado implements NotificacaoClientePort {

  private static final Logger log = LoggerFactory.getLogger(EmailSmtpAdapterSimulado.class);

  private static final String SERVIDOR = "smtp.simulado.local:587";
  private static final String REMETENTE = "faturamento@loja.com.br";

  @Override
  public String notificar(Fatura fatura, Pedido pedido) {
    String assunto = "Sua fatura do pedido #" + pedido.pedidoId() + " foi emitida";
    String corpo = """
        Olá, %s!

        A fatura %s do seu pedido #%d, no valor de R$ %s, foi emitida.

        Código de barras do boleto: %s
        Chave de acesso da NF-e: %s

        Obrigado pela compra!""".formatted(pedido.cliente(), fatura.getId(), pedido.pedidoId(),
        fatura.getValor(), fatura.getCodigoBarrasBoleto(), fatura.getChaveAcessoNotaFiscal());

    String conteudo = "From: " + REMETENTE + "\r\n"
        + "To: " + pedido.emailCliente() + "\r\n"
        + "Subject: " + assunto + "\r\n\r\n"
        + corpo;

    log.info("[SMTP simulado] conectando em {}", SERVIDOR);
    log.info("[SMTP simulado] EHLO hub / MAIL FROM:<{}> / RCPT TO:<{}> / DATA", REMETENTE,
        pedido.emailCliente());
    log.info("[SMTP simulado] conteúdo:\n{}", conteudo);

    String hash = sha256(conteudo);
    log.info("[SMTP simulado] 250 OK - mensagem aceita. Hash SHA-256 do conteúdo: {}", hash);
    return hash;
  }

  private static String sha256(String texto) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(texto.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 indisponível", e);
    }
  }
}
