package br.com.hub.application.service;

import br.com.hub.application.port.in.ConsultarFaturaUseCase;
import br.com.hub.application.port.in.FaturarPedidoUseCase;
import br.com.hub.application.port.out.EmissaoBoletoPort;
import br.com.hub.application.port.out.EmissaoNotaFiscalPort;
import br.com.hub.application.port.out.FaturaRepositoryPort;
import br.com.hub.application.port.out.NotificacaoClientePort;
import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.Pedido;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso de faturamento. Conhece apenas as portas (interfaces),
 * nunca os adaptadores concretos. Não tem anotação do Spring: o bean
 * é criado em {@code config.HubConfig}.
 */
public class FaturamentoService implements FaturarPedidoUseCase, ConsultarFaturaUseCase {

  private final FaturaRepositoryPort faturaRepository;
  private final EmissaoBoletoPort emissaoBoleto;
  private final EmissaoNotaFiscalPort emissaoNotaFiscal;
  private final NotificacaoClientePort notificacaoCliente;

  public FaturamentoService(FaturaRepositoryPort faturaRepository, EmissaoBoletoPort emissaoBoleto,
      EmissaoNotaFiscalPort emissaoNotaFiscal, NotificacaoClientePort notificacaoCliente) {
    this.faturaRepository = faturaRepository;
    this.emissaoBoleto = emissaoBoleto;
    this.emissaoNotaFiscal = emissaoNotaFiscal;
    this.notificacaoCliente = notificacaoCliente;
  }

  @Override
  public UUID faturar(Pedido pedido) {
    // RF01 - cria e persiste a fatura
    Fatura fatura = faturaRepository.salvar(Fatura.criar(pedido));

    // RF02 - emite o boleto (fila) e guarda o código de barras
    fatura.registrarBoleto(emissaoBoleto.emitir(fatura));
    faturaRepository.salvar(fatura);

    // RF03 - emite a nota fiscal (SOAP) e guarda a chave de acesso de 44 dígitos
    fatura.registrarNotaFiscal(emissaoNotaFiscal.emitir(fatura, pedido));
    faturaRepository.salvar(fatura);

    // RF04 - notifica o cliente (SMTP) e guarda o hash do conteúdo do e-mail
    fatura.registrarEnvioEmail(notificacaoCliente.notificar(fatura, pedido));
    faturaRepository.salvar(fatura);

    // RF01 - devolve o ID para a camada REST
    return fatura.getId();
  }

  @Override
  public Optional<Fatura> buscar(UUID faturaId) {
    return faturaRepository.buscarPorId(faturaId);
  }
}
