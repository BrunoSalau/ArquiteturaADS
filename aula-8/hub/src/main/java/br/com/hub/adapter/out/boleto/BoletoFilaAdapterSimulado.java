package br.com.hub.adapter.out.boleto;

import br.com.hub.application.port.out.EmissaoBoletoPort;
import br.com.hub.domain.model.Fatura;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptador SIMULADO de emissão de boleto por FILA.
 * Em produção publicaria a mensagem num broker (RabbitMQ, SQS, Kafka...) e
 * leria a resposta do serviço bancário. Aqui a publicação é só registrada
 * no log e o "consumidor" devolve um código de barras FEBRABAN de 44 dígitos.
 */
@Component
public class BoletoFilaAdapterSimulado implements EmissaoBoletoPort {

  private static final Logger log = LoggerFactory.getLogger(BoletoFilaAdapterSimulado.class);

  static final String FILA = "hub.boletos.emitir";
  private static final String BANCO = "001";
  private static final String MOEDA = "9";
  private static final int DIAS_VENCIMENTO = 3;

  @Override
  public String emitir(Fatura fatura) {
    String mensagem = """
        {"faturaId":"%s","pedidoId":%d,"sacado":"%s","valor":%s}"""
        .formatted(fatura.getId(), fatura.getPedidoId(), fatura.getCliente(), fatura.getValor());
    log.info("[FILA simulada] publicando na fila '{}': {}", FILA, mensagem);

    String codigoBarras = gerarCodigoBarras(fatura.getValor(), LocalDate.now().plusDays(DIAS_VENCIMENTO));

    log.info("[FILA simulada] retorno do emissor de boletos -> código de barras {}", codigoBarras);
    return codigoBarras;
  }

  /** Banco(3) + Moeda(1) + DV(1) + Fator vencimento(4) + Valor(10) + Campo livre(25) = 44. */
  static String gerarCodigoBarras(BigDecimal valor, LocalDate vencimento) {
    String fator = String.format("%04d", fatorVencimento(vencimento));
    long centavos = valor.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    String valorFormatado = String.format("%010d", centavos);
    String campoLivre = digitosAleatorios(25);

    String semDv = BANCO + MOEDA + fator + valorFormatado + campoLivre;
    int dv = digitoVerificadorModulo11(semDv);
    return BANCO + MOEDA + dv + fator + valorFormatado + campoLivre;
  }

  /** Fator FEBRABAN: reiniciou em 1000 no dia 22/02/2025. */
  private static long fatorVencimento(LocalDate vencimento) {
    LocalDate base = LocalDate.of(2025, 2, 22);
    if (vencimento.isBefore(base)) {
      return ChronoUnit.DAYS.between(LocalDate.of(1997, 10, 7), vencimento);
    }
    return 1000 + ChronoUnit.DAYS.between(base, vencimento) % 9000;
  }

  private static int digitoVerificadorModulo11(String numero) {
    int soma = 0;
    int peso = 2;
    for (int i = numero.length() - 1; i >= 0; i--) {
      soma += (numero.charAt(i) - '0') * peso;
      peso = peso == 9 ? 2 : peso + 1;
    }
    int dv = 11 - (soma % 11);
    return (dv == 0 || dv == 10 || dv == 11) ? 1 : dv;
  }

  private static String digitosAleatorios(int quantidade) {
    StringBuilder sb = new StringBuilder(quantidade);
    for (int i = 0; i < quantidade; i++) {
      sb.append(ThreadLocalRandom.current().nextInt(10));
    }
    return sb.toString();
  }
}
