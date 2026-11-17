package br.com.mvc.service;

import br.com.mvc.model.MovimentacaoEstoque;
import br.com.mvc.model.Produto;
import br.com.mvc.model.StatusProduto;
import br.com.mvc.model.TipoMovimentacao;
import br.com.mvc.repository.MovimentacaoEstoqueRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras do estoque:
 * - produto DESCONTINUADO não pode ser movimentado;
 * - quando o saldo sai de zero, o produto fica DISPONÍVEL;
 * - quando o saldo zera, o produto fica INDISPONÍVEL;
 * - não é permitida saída maior que o saldo.
 */
@Service
public class EstoqueService {

  private final ProdutoService produtoService;
  private final MovimentacaoEstoqueRepository movimentacaoRepository;

  public EstoqueService(ProdutoService produtoService,
      MovimentacaoEstoqueRepository movimentacaoRepository) {
    this.produtoService = produtoService;
    this.movimentacaoRepository = movimentacaoRepository;
  }

  public List<MovimentacaoEstoque> listarMovimentacoes() {
    return movimentacaoRepository.findAllByOrderByDataHoraDesc();
  }

  @Transactional
  public MovimentacaoEstoque registrarEntrada(Long produtoId, Integer quantidade, String observacao) {
    validarQuantidade(quantidade);
    Produto produto = produtoService.buscar(produtoId);
    validarNaoDescontinuado(produto);

    int saldoAnterior = produto.getQuantidadeEstoque();
    int novoSaldo = saldoAnterior + quantidade;
    produto.setQuantidadeEstoque(novoSaldo);

    if (saldoAnterior == 0) {
      produto.setStatus(StatusProduto.DISPONIVEL);
    }

    return movimentacaoRepository.save(
        new MovimentacaoEstoque(produto, TipoMovimentacao.ENTRADA, quantidade, novoSaldo, observacao));
  }

  @Transactional
  public MovimentacaoEstoque registrarSaida(Long produtoId, Integer quantidade, String observacao) {
    validarQuantidade(quantidade);
    Produto produto = produtoService.buscar(produtoId);
    validarNaoDescontinuado(produto);

    int saldoAnterior = produto.getQuantidadeEstoque();
    if (quantidade > saldoAnterior) {
      throw new RegraNegocioException("Estoque insuficiente para \"" + produto.getNome()
          + "\". Saldo atual: " + saldoAnterior + ".");
    }

    int novoSaldo = saldoAnterior - quantidade;
    produto.setQuantidadeEstoque(novoSaldo);

    if (novoSaldo == 0) {
      produto.setStatus(StatusProduto.INDISPONIVEL);
    }

    return movimentacaoRepository.save(
        new MovimentacaoEstoque(produto, TipoMovimentacao.SAIDA, quantidade, novoSaldo, observacao));
  }

  private void validarQuantidade(Integer quantidade) {
    if (quantidade == null || quantidade <= 0) {
      throw new RegraNegocioException("A quantidade deve ser maior que zero.");
    }
  }

  private void validarNaoDescontinuado(Produto produto) {
    if (produto.isDescontinuado()) {
      throw new RegraNegocioException("O produto \"" + produto.getNome()
          + "\" está descontinuado e não pode ser movimentado no estoque.");
    }
  }
}
