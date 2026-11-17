package br.com.mvc.controller;

import br.com.mvc.service.EstoqueService;
import br.com.mvc.service.ProdutoService;
import br.com.mvc.service.RegraNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {

  private final EstoqueService estoqueService;
  private final ProdutoService produtoService;

  public EstoqueController(EstoqueService estoqueService, ProdutoService produtoService) {
    this.estoqueService = estoqueService;
    this.produtoService = produtoService;
  }

  @GetMapping
  public String tela(Model model) {
    model.addAttribute("produtos", produtoService.listarManipulaveisNoEstoque());
    model.addAttribute("movimentacoes", estoqueService.listarMovimentacoes());
    return "estoque/estoque";
  }

  @PostMapping("/entrada")
  public String entrada(@RequestParam Long produtoId, @RequestParam Integer quantidade,
      @RequestParam(required = false) String observacao, RedirectAttributes redirect) {
    try {
      estoqueService.registrarEntrada(produtoId, quantidade, observacao);
      redirect.addFlashAttribute("sucesso", "Entrada registrada.");
    } catch (RegraNegocioException e) {
      redirect.addFlashAttribute("erro", e.getMessage());
    }
    return "redirect:/estoque";
  }

  @PostMapping("/saida")
  public String saida(@RequestParam Long produtoId, @RequestParam Integer quantidade,
      @RequestParam(required = false) String observacao, RedirectAttributes redirect) {
    try {
      estoqueService.registrarSaida(produtoId, quantidade, observacao);
      redirect.addFlashAttribute("sucesso", "Saída registrada.");
    } catch (RegraNegocioException e) {
      redirect.addFlashAttribute("erro", e.getMessage());
    }
    return "redirect:/estoque";
  }
}
