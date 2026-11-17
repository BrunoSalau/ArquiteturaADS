package br.com.mvc.controller;

import br.com.mvc.model.Produto;
import br.com.mvc.service.ProdutoService;
import br.com.mvc.service.RegraNegocioException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

  private final ProdutoService produtoService;

  public ProdutoController(ProdutoService produtoService) {
    this.produtoService = produtoService;
  }

  @GetMapping
  public String listar(Model model) {
    model.addAttribute("produtos", produtoService.listar());
    return "produtos/lista";
  }

  @GetMapping("/novo")
  public String novo(Model model) {
    model.addAttribute("produto", new Produto());
    return "produtos/form";
  }

  @PostMapping
  public String criar(@ModelAttribute Produto produto, Model model, RedirectAttributes redirect) {
    try {
      produtoService.criar(produto);
      redirect.addFlashAttribute("sucesso", "Produto cadastrado como INDISPONÍVEL (sem estoque).");
      return "redirect:/produtos";
    } catch (RegraNegocioException e) {
      model.addAttribute("erro", e.getMessage());
      model.addAttribute("produto", produto);
      return "produtos/form";
    }
  }

  @GetMapping("/{id}/editar")
  public String editar(@PathVariable Long id, Model model) {
    model.addAttribute("produto", produtoService.buscar(id));
    return "produtos/form";
  }

  @PostMapping("/{id}")
  public String atualizar(@PathVariable Long id, @ModelAttribute Produto produto, Model model,
      RedirectAttributes redirect) {
    try {
      produtoService.atualizar(id, produto);
      redirect.addFlashAttribute("sucesso", "Produto atualizado.");
      return "redirect:/produtos";
    } catch (RegraNegocioException e) {
      produto.setId(id);
      model.addAttribute("erro", e.getMessage());
      model.addAttribute("produto", produto);
      return "produtos/form";
    }
  }

  @PostMapping("/{id}/descontinuar")
  public String descontinuar(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
    try {
      produtoService.descontinuar(id, PerfilAdvice.perfilDaSessao(session));
      redirect.addFlashAttribute("sucesso", "Produto descontinuado.");
    } catch (RegraNegocioException e) {
      redirect.addFlashAttribute("erro", e.getMessage());
    }
    return "redirect:/produtos";
  }
}
