package br.com.mvc.controller;

import br.com.mvc.model.Perfil;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Disponibiliza o perfil atual (guardado na sessão) para todas as telas. */
@ControllerAdvice
public class PerfilAdvice {

  public static final String ATRIBUTO_SESSAO = "perfil";

  @ModelAttribute("perfilAtual")
  public Perfil perfilAtual(HttpSession session) {
    return perfilDaSessao(session);
  }

  @ModelAttribute("perfis")
  public Perfil[] perfis() {
    return Perfil.values();
  }

  public static Perfil perfilDaSessao(HttpSession session) {
    Object valor = session.getAttribute(ATRIBUTO_SESSAO);
    return valor instanceof Perfil perfil ? perfil : Perfil.OPERADOR;
  }
}
