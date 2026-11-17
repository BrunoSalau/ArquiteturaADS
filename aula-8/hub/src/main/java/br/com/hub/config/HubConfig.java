package br.com.hub.config;

import br.com.hub.application.port.out.EmissaoBoletoPort;
import br.com.hub.application.port.out.EmissaoNotaFiscalPort;
import br.com.hub.application.port.out.FaturaRepositoryPort;
import br.com.hub.application.port.out.NotificacaoClientePort;
import br.com.hub.application.service.FaturamentoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Liga o núcleo (casos de uso) aos adaptadores, mantendo o núcleo livre do Spring. */
@Configuration
public class HubConfig {

  @Bean
  public FaturamentoService faturamentoService(FaturaRepositoryPort faturaRepository,
      EmissaoBoletoPort emissaoBoleto, EmissaoNotaFiscalPort emissaoNotaFiscal,
      NotificacaoClientePort notificacaoCliente) {
    return new FaturamentoService(faturaRepository, emissaoBoleto, emissaoNotaFiscal,
        notificacaoCliente);
  }
}
