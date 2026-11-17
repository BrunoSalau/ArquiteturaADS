package br.com.mvc.service;

import br.com.mvc.integracao.hub.HubFaturamentoClient;
import br.com.mvc.integracao.hub.HubFaturamentoException;
import br.com.mvc.model.ItemPedido;
import br.com.mvc.model.Pedido;
import br.com.mvc.model.Produto;
import br.com.mvc.model.StatusProduto;
import br.com.mvc.repository.PedidoRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

  private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

  private final PedidoRepository pedidoRepository;
  private final ProdutoService produtoService;
  private final EstoqueService estoqueService;
  private final HubFaturamentoClient hubFaturamento;

  public PedidoService(PedidoRepository pedidoRepository, ProdutoService produtoService,
      EstoqueService estoqueService, HubFaturamentoClient hubFaturamento) {
    this.pedidoRepository = pedidoRepository;
    this.produtoService = produtoService;
    this.estoqueService = estoqueService;
    this.hubFaturamento = hubFaturamento;
  }

  public List<Pedido> listar() {
    return pedidoRepository.findAllByOrderByIdDesc();
  }

  public Pedido buscar(Long id) {
    return pedidoRepository.findById(id)
        .orElseThrow(() -> new RegraNegocioException("Pedido não encontrado: " + id));
  }

  @Transactional
  public Pedido criar(String cliente, String emailCliente) {
    if (cliente == null || cliente.isBlank()) {
      throw new RegraNegocioException("Informe o cliente.");
    }
    if (emailCliente == null || !emailCliente.contains("@")) {
      throw new RegraNegocioException("Informe um e-mail válido para o cliente.");
    }
    return pedidoRepository.save(new Pedido(cliente.trim(), emailCliente.trim()));
  }

  @Transactional
  public void adicionarItem(Long pedidoId, Long produtoId, Integer quantidade) {
    Pedido pedido = buscarAberto(pedidoId);
    if (quantidade == null || quantidade <= 0) {
      throw new RegraNegocioException("A quantidade deve ser maior que zero.");
    }
    Produto produto = produtoService.buscar(produtoId);
    if (produto.getStatus() != StatusProduto.DISPONIVEL) {
      throw new RegraNegocioException("O produto \"" + produto.getNome() + "\" não está disponível para venda.");
    }
    pedido.adicionarItem(produto, quantidade);
  }

  @Transactional
  public void removerItem(Long pedidoId, Long itemId) {
    buscarAberto(pedidoId).removerItem(itemId);
  }

  /**
   * Fecha o pedido: baixa o estoque, envia o pedido ao hub e guarda o ID da fatura.
   * Se o hub falhar, o pedido continua FECHADO, sem fatura, e pode ser reenviado.
   */
  @Transactional
  public Pedido fechar(Long pedidoId) {
    Pedido pedido = buscarAberto(pedidoId);
    if (pedido.getItens().isEmpty()) {
      throw new RegraNegocioException("Inclua ao menos um item antes de fechar o pedido.");
    }

    for (ItemPedido item : pedido.getItens()) {
      estoqueService.registrarSaida(item.getProduto().getId(), item.getQuantidade(),
          "Venda - pedido #" + pedido.getId());
    }
    pedido.fechar();

    enviarAoHub(pedido);
    return pedido;
  }

  /** Reenvia ao hub um pedido fechado que ficou sem fatura. */
  @Transactional
  public Pedido reenviarAoHub(Long pedidoId) {
    Pedido pedido = buscar(pedidoId);
    if (!pedido.isAguardandoFatura()) {
      throw new RegraNegocioException("Só é possível reenviar pedidos fechados que ainda não têm fatura.");
    }
    enviarAoHub(pedido);
    return pedido;
  }

  private void enviarAoHub(Pedido pedido) {
    try {
      String faturaId = hubFaturamento.faturar(pedido);
      pedido.registrarFatura(faturaId);
    } catch (HubFaturamentoException e) {
      log.warn("Pedido #{} fechado sem fatura: {}", pedido.getId(), e.getMessage());
      pedido.registrarErroIntegracao(e.getMessage());
    }
  }

  private Pedido buscarAberto(Long pedidoId) {
    Pedido pedido = buscar(pedidoId);
    if (!pedido.isAberto()) {
      throw new RegraNegocioException("O pedido #" + pedidoId + " já foi fechado.");
    }
    return pedido;
  }
}
