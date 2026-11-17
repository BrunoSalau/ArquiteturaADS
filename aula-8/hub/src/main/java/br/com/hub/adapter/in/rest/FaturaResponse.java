package br.com.hub.adapter.in.rest;

import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.StatusFatura;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FaturaResponse(UUID faturaId, Long pedidoId, String cliente, String emailCliente,
    BigDecimal valor, StatusFatura status, String codigoBarrasBoleto, String chaveAcessoNotaFiscal,
    String hashEmail, LocalDateTime criadaEm) {

  public static FaturaResponse de(Fatura f) {
    return new FaturaResponse(f.getId(), f.getPedidoId(), f.getCliente(), f.getEmailCliente(),
        f.getValor(), f.getStatus(), f.getCodigoBarrasBoleto(), f.getChaveAcessoNotaFiscal(),
        f.getHashEmail(), f.getCriadaEm());
  }
}
