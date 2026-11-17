package br.com.mvc.service;

import br.com.mvc.model.Perfil;
import br.com.mvc.model.Produto;
import br.com.mvc.model.StatusProduto;
import br.com.mvc.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {

  private final ProdutoRepository produtoRepository;

  public ProdutoService(ProdutoRepository produtoRepository) {
    this.produtoRepository = produtoRepository;
  }

  public List<Produto> listar() {
    return produtoRepository.findAllByOrderByNomeAsc();
  }

  /** Produtos que podem ser movimentados no estoque (todos, menos os descontinuados). */
  public List<Produto> listarManipulaveisNoEstoque() {
    return produtoRepository.findByStatusNotOrderByNomeAsc(StatusProduto.DESCONTINUADO);
  }

  public Produto buscar(Long id) {
    return produtoRepository.findById(id)
        .orElseThrow(() -> new RegraNegocioException("Produto não encontrado: " + id));
  }

  /** Todo produto novo começa INDISPONÍVEL e com estoque zero. */
  @Transactional
  public Produto criar(Produto produto) {
    validar(produto);
    produto.setId(null);
    produto.setQuantidadeEstoque(0);
    produto.setStatus(StatusProduto.INDISPONIVEL);
    return produtoRepository.save(produto);
  }

  /** Edita só os dados cadastrais; status e estoque mudam pelas regras próprias. */
  @Transactional
  public Produto atualizar(Long id, Produto dados) {
    validar(dados);
    Produto produto = buscar(id);
    produto.setNome(dados.getNome());
    produto.setDescricao(dados.getDescricao());
    produto.setPreco(dados.getPreco());
    return produto;
  }

  /** Só o administrador pode descontinuar um produto. */
  @Transactional
  public Produto descontinuar(Long id, Perfil perfil) {
    if (perfil != Perfil.ADM) {
      throw new RegraNegocioException("Apenas o administrador pode descontinuar um produto.");
    }
    Produto produto = buscar(id);
    if (produto.isDescontinuado()) {
      throw new RegraNegocioException("O produto já está descontinuado.");
    }
    produto.setStatus(StatusProduto.DESCONTINUADO);
    return produto;
  }

  private void validar(Produto produto) {
    if (produto.getNome() == null || produto.getNome().isBlank()) {
      throw new RegraNegocioException("Informe o nome do produto.");
    }
    if (produto.getPreco() == null || produto.getPreco().compareTo(BigDecimal.ZERO) < 0) {
      throw new RegraNegocioException("Informe um preço válido.");
    }
  }
}
