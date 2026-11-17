package br.com.mvc.config;

import br.com.mvc.model.Produto;
import br.com.mvc.service.EstoqueService;
import br.com.mvc.service.ProdutoService;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Cadastra alguns produtos de exemplo ao subir a aplicação (somente quando o banco está vazio). */
@Component
public class DadosIniciais implements CommandLineRunner {

  private final ProdutoService produtoService;
  private final EstoqueService estoqueService;

  public DadosIniciais(ProdutoService produtoService, EstoqueService estoqueService) {
    this.produtoService = produtoService;
    this.estoqueService = estoqueService;
  }

  @Override
  public void run(String... args) {
    if (!produtoService.listar().isEmpty()) {
      return;
    }
    Produto teclado = produtoService.criar(
        new Produto("Teclado mecânico", "Switch azul, ABNT2", new BigDecimal("249.90")));
    Produto mouse = produtoService.criar(
        new Produto("Mouse sem fio", "1600 DPI", new BigDecimal("89.90")));
    produtoService.criar(
        new Produto("Monitor 24\"", "Full HD, 75 Hz", new BigDecimal("799.00")));

    estoqueService.registrarEntrada(teclado.getId(), 10, "Carga inicial");
    estoqueService.registrarEntrada(mouse.getId(), 5, "Carga inicial");
  }
}
