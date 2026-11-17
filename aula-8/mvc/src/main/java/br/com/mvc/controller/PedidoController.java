package br.com.mvc.controller;

import br.com.mvc.model.Pedido;
import br.com.mvc.model.StatusProduto;
import br.com.mvc.service.PedidoService;
import br.com.mvc.service.ProdutoService;
import br.com.mvc.service.RegraNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

  private final PedidoService pedidoService;
  private final ProdutoService produtoService;

  public PedidoController(PedidoService pedidoService, ProdutoService produtoService) {
    this.pedidoService = pedidoService;
    this.produtoService = produtoService;
  }

  @GetMapping
  public String listar(Model model) {
    model.addAttribute("pedidos", pedidoService.listar());
    return "pedidos/lista";
  }

  @PostMapping
  public String criar(@RequestParam String cliente, @RequestParam String emailCliente,
      RedirectAttributes redirect) {
    try {
      Pedido pedido = pedidoService.criar(cliente, emailCliente);
      return "redirect:/pedidos/" + pedido.getId();
    } catch (RegraNegocioException e) {
      redirect.addFlashAttribute("erro", e.getMessage());
      return "redirect:/pedidos";
    }
  }

  @GetMapping("/{id}")
  public String detalhe(@PathVariable Long id, Model model) {
    model.addAttribute("pedido", pedidoService.buscar(id));
    model.addAttribute("produtos", produtoService.listar().stream()
        .filter(p -> p.getStatus() == StatusProduto.DISPONIVEL)
        .toList());
    return "pedidos/detalhe";
  }

  @PostMapping("/{id}/itens")
  public String adicionarItem(@PathVariable Long id, @RequestParam Long produtoId,
      @RequestParam Integer quantidade, RedirectAttributes redirect) {
    try {
      pedidoService.adicionarItem(id, produtoId, quantidade);
    } catch (RegraNegocioException e) {
      redirect.addFlashAttribute("erro", e.getMessage());
    }
    return "redirect:/pedidos/" + id;
  }

  @PostMapping("/{id}/itens/{itemId}/remover")
  public String removerItem(@PathVariable Long id, @PathVariable Long itemId,
      RedirectAttributes redirect) {
    try {
      pedidoService.removerItem(id, itemId);
    } catch (RegraNegocioException e) {
      redirect.addFlashAttribute("erro", e.getMessage());
    }
    return "redirect:/pedidos/" + id;
  }

  @PostMapping("/{id}/fechar")
  public String fechar(@PathVariable Long id, RedirectAttributes redirect) {
    try {
      Pedido pedido = pedidoService.fechar(id);
      mensagemDoFaturamento(pedido, redirect);
    } catch (RegraNegocioException e) {
      redirect.addFlashAttribute("erro", e.getMessage());
    }
    return "redirect:/pedidos/" + id;
  }

  @PostMapping("/{id}/reenviar")
  public String reenviar(@PathVariable Long id, RedirectAttributes redirect) {
    try {
      Pedido pedido = pedidoService.reenviarAoHub(id);
      mensagemDoFaturamento(pedido, redirect);
    } catch (RegraNegocioException e) {
      redirect.addFlashAttribute("erro", e.getMessage());
    }
    return "redirect:/pedidos/" + id;
  }

  private void mensagemDoFaturamento(Pedido pedido, RedirectAttributes redirect) {
    if (pedido.getFaturaId() != null) {
      redirect.addFlashAttribute("sucesso", "Pedido fechado e faturado. Fatura: " + pedido.getFaturaId());
    } else {
      redirect.addFlashAttribute("erro", "Pedido fechado, mas o hub não devolveu a fatura: "
          + pedido.getErroIntegracao() + " Confira no hub se a fatura já existe antes de reenviar.");
    }
  }
}
